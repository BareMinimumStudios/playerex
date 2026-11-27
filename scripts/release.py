"""Validate release metadata and stage only verified production artifacts."""
from pathlib import Path
import argparse
import datetime
import hashlib
import io
import json
import os
import re
import shutil
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
MC = "1.21.1"
REMNANT_SHA512 = {'fabric': '4bd42c30e0ab3be48ba18c4858fbc659e3b90d4fb057b30b2fd8be5cfc806478570b2eb481422153be30d551a3358a08720e837e2617a163bc408a8737bdf3ed', 'neoforge': '87e634e5a0306dfded3f1a44f32e3533d69d7f36f769e5e73e895ebee4a7578619a2511fbfed4dcf9e14e48719dc8f9b05138dab52dedf7afa47b5cc79fbb4ec'}


def release_metadata():
    properties = dict(re.findall(r"^([a-z_]+)=(.+)$", (ROOT / "gradle.properties").read_text(), re.M))
    version = properties["mod_version"].strip()
    if not re.fullmatch(r"(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)", version):
        raise ValueError("Release version must be a plain major.minor.patch value")
    tag = f"v{version}"
    if os.environ.get("GITHUB_REF_TYPE") == "tag" and os.environ.get("GITHUB_REF_NAME") != tag:
        raise ValueError(f"Release tag must match {tag}")
    text = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
    headings = list(re.finditer(r"^## (.+)$", text, re.M))
    if not headings or headings[0][1] != "[Unreleased]":
        raise ValueError("CHANGELOG.md must begin its release sections with [Unreleased]")
    expected = re.compile(r"\[" + re.escape(version) + r"\] - (\d{4}-\d{2}-\d{2})")
    if len(headings) < 2 or not (current := expected.fullmatch(headings[1][1])):
        raise ValueError(f"The first dated changelog section must be [{version}] - YYYY-MM-DD")
    if sum(expected.fullmatch(heading[1]) is not None for heading in headings) != 1:
        raise ValueError(f"Duplicate [{version}] changelog section")
    date = datetime.date.fromisoformat(current[1])
    if date > datetime.date.today():
        raise ValueError("Release date is in the future")

    end = headings[2].start() if len(headings) > 2 else len(text)
    notes = text[headings[1].end():end].strip()
    # The oldest section may be followed by the changelog's comparison links.
    notes = re.split(r"^\[(?:Unreleased|\d+\.\d+\.\d+)\]:", notes, maxsplit=1, flags=re.M)[0].strip()
    categories = re.split(r"^### (.+)$", notes, flags=re.M)
    allowed = {"Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"}
    seen = set()
    for name, changes in zip(categories[1::2], categories[2::2]):
        if name not in allowed or name in seen:
            raise ValueError(f"Invalid or duplicate changelog category: {name}")
        if not re.search(r"^[-*] \S", changes, re.M):
            raise ValueError(f"Changelog category {name} has no entries")
        seen.add(name)
    if not seen:
        raise ValueError("Release notes must contain Keep a Changelog categories")
    # Publishing hosts render this file outside CHANGELOG.md.
    return version, tag, f"## {version} - {date.isoformat()}\n\n{notes}\n"


def verify_jar(path, loader, version):
    with zipfile.ZipFile(path) as jar:
        names = jar.namelist()
        if jar.testzip():
            raise ValueError(f"Invalid archive: {path}")
        if any("RelicProbe" in n or n.startswith("net/bms/remnant/") for n in names):
            raise ValueError(f"Test code or shaded Remnant in {path}")
        adapter = "com/bibireden/playerex/compat/iron/NeoForgeIronSpells.class"
        assert (adapter in names) == (loader == "neoforge"), "Iron adapter must be NeoForge-only"
        assert not any(n.startswith("io/redspace/ironsspellbooks/") for n in names), "Iron classes must not be shaded"
        nested = []
        for name in names:
            if name.endswith(".jar"):
                with zipfile.ZipFile(io.BytesIO(jar.read(name))) as dependency:
                    assert not any(n.startswith("io/redspace/ironsspellbooks/") for n in dependency.namelist()), "Iron must not be bundled"
                    if "net/bms/remnant/api/PlayerLedger.class" in dependency.namelist():
                        nested.append(name)
        if len(nested) != 1:
            raise ValueError(f"Expected one nested Remnant in {path}")
        raw = jar.read(nested[0])
        if hashlib.sha512(raw).hexdigest() != REMNANT_SHA512[loader]:
            raise ValueError(f"Nested Remnant differs from the verified public {loader} release")
        with zipfile.ZipFile(io.BytesIO(raw)) as dep:
            if loader == "fabric":
                metadata = json.loads(jar.read("fabric.mod.json"))
                assert metadata["id"] == "playerex" and metadata["version"] == version
                assert "irons_spellbooks" not in metadata.get("depends", {})
                assert "irons_spellbooks" not in metadata.get("suggests", {})
                assert metadata["depends"]["remnant"] == ">=3.0.0"
                assert metadata["depends"]["data_attributes"] == ">=3.0.0"
                assert nested[0] in [item["file"] for item in metadata["jars"]]
                assert json.loads(dep.read("fabric.mod.json"))["version"] == "3.0.0"
            else:
                metadata = tomllib.loads(jar.read("META-INF/neoforge.mods.toml").decode())
                assert any(m["modId"] == "playerex" and m["version"] == version for m in metadata["mods"])
                dependencies = {d["modId"]: d for d in metadata["dependencies"]["playerex"]}
                assert dependencies["irons_spellbooks"]["type"] == "optional"
                assert dependencies["irons_spellbooks"]["versionRange"] == "[1.21.1-3.16.4,)"
                for name in ("remnant", "data_attributes"):
                    assert dependencies[name]["type"] == "required"
                    assert dependencies[name]["versionRange"] == "[3.0.0,)"
                nested_metadata = json.loads(jar.read("META-INF/jarjar/metadata.json"))
                assert nested[0] in [item["path"] for item in nested_metadata["jars"]]
                assert tomllib.loads(dep.read("META-INF/neoforge.mods.toml").decode())["mods"][0]["version"] == "3.0.0"
        for name in names:
            if name.endswith(".json"):
                value = json.loads(jar.read(name))
                if "/lang/" in name:
                    assert isinstance(value, dict) and all(isinstance(v, str) for v in value.values())
        assert "META-INF/licenses/Remnant-LICENSE" in names


def stage(version, notes):
    destination = ROOT / "build/release"
    destination.mkdir(parents=True, exist_ok=True)
    for loader in ("fabric", "neoforge"):
        path = ROOT / f"build/libs/PlayerEx-{version}-{loader}-{MC}.jar"
        verify_jar(path, loader, version)
        target = destination / path.name
        shutil.copy2(path, target)
        target.with_suffix(".jar.sha256").write_text(hashlib.sha256(target.read_bytes()).hexdigest() + "  " + target.name + "\n")
        print(f"Verified {loader}: {target.name}")
    (destination / "RELEASE-NOTES.md").write_text(notes, encoding="utf-8")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--stage", action="store_true")
    args = parser.parse_args()
    version, tag, notes = release_metadata()
    if args.stage:
        stage(version, notes)
    else:
        print(f"version={version}\ntag={tag}")
        if os.environ.get("GITHUB_OUTPUT"):
            with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as output:
                output.write(f"version={version}\ntag={tag}\n")
