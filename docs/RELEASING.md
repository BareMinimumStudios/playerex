# Preparing a release

PlayerEx builds both Minecraft 1.21.1 loaders from the same source. The public version is **5.0.0**; the mod metadata has no alpha, beta or Minecraft suffix.

## Before publishing

Use Java 21 and the included wrapper. Run `./gradlew clean build --warning-mode all` on Linux/macOS or `.\gradlew.bat clean build --warning-mode all` on Windows. Then run `python3 scripts/release.py --stage` (or `python` on Windows).

The release script checks the version, dated changelog section, loader metadata, required dependencies, vanilla language files and the exact bytes of the public nested Remnant JAR. It also rejects test probes and shaded Remnant classes. It stages only the two production JARs, SHA-256 checksums and the current release notes under `build/release/`.

Keep configuration caching and parallel project execution disabled in `gradle.properties`. The current Cloche NeoForge patcher resolves dependencies during task execution; enabling configuration caching can make a fresh runner fail with an exclusive-lock error. Gradle's normal build cache remains enabled.

The build resolves public dependencies through Modrinth Maven. `libraries.toml` pins Data Attributes 3.0.0 to `tyrHzydP` (Fabric) and `hLnhUxoL` (NeoForge), and Remnant 3.0.0 to `7AYXvK88` (Fabric) and `Twla1z8f` (NeoForge). When updating Remnant, update the expected SHA-512 values in `scripts/release.py` from the public Modrinth version metadata as well. A dependency update needs a build and runtime check on both loaders.

Before calling a build ready, check the GUI, tooltips, armor and connected-player lifecycle in-game. Compilation and a dedicated-server test cannot verify those client behaviors.

## GitHub Actions

The build workflow runs for pull requests and pushes to `main`, `1.21.1` and branches below `1.21.1/`. It compiles both loaders, runs shared checks and uploads the production JARs without publishing them.

The publish workflow follows Remnant's mc-publish setup: one verified build, separate loader/platform jobs and a GitHub release job. Actions are pinned to commit hashes. Marketplace jobs have read-only repository permissions; only the GitHub release job can write a release.

Add these repository or organization Actions secrets:

- `MODRINTH_TOKEN`: permission to upload to PlayerEx DC, project `4UKlJSdk`.
- `CURSEFORGE_TOKEN`: permission to upload to PlayerEx DC, project `958325`.

`GITHUB_TOKEN` is supplied by GitHub. No token belongs in source files.

Publishing starts when a matching tag is pushed, or when an authorized maintainer runs the publish workflow manually. For this version, the tag is **`v5.0.0`**. A mismatched tag or missing dated changelog section stops the build before upload.

Modrinth and CurseForge receive separate Fabric and NeoForge files. Their version identifiers are `5.0.0+1.21.1-fabric` and `5.0.0+1.21.1-neoforge`, including the Minecraft version and loader; the installed mod version remains `5.0.0`. Both are marked **release**, for Minecraft 1.21.1, Java 21, client and server.

Data Attributes, Fzzy Config and the loader's language/runtime dependencies are marked required. Remnant is marked embedded because PlayerEx already includes its public JAR. Players should not install a second copy just to satisfy the dependency listing.

For a partial failure, run the workflow again at the **same tag/commit** and choose only the failed target: `fabric-modrinth`, `fabric-curseforge`, `neoforge-modrinth`, `neoforge-curseforge` or `github`. Re-running `all` can duplicate files that were already uploaded.

The GitHub release contains both JARs and the current changelog section. GitHub also provides source downloads for the tagged commit. Local source delivery should exclude build caches, run worlds, private instructions and temporary tests.

## Changelog and descriptions

Keep future notes under `[Unreleased]`. Before a release, move them into `## [version] - YYYY-MM-DD`, using only the relevant Added, Changed, Deprecated, Removed, Fixed or Security sections. `scripts/release.py` extracts that release alone for every destination.

`README.md` is the GitHub overview. `docs/MODRINTH.md` and `docs/CURSEFORGE.md` provide ready-to-paste marketplace descriptions. Keep both aligned with the player-facing content in the README. They use ordinary Markdown and clickable image buttons.

Screenshots live in `docs/images/`. Attributes is the main image; Combat Stats and Spell Schools appear smaller below it. The README uses repository-relative links. Marketplace descriptions use raw GitHub URLs targeting `1.21.1`, where the images are currently published. Keep `docs/images/` and `docs/branding/` on that branch and keep their filenames stable. If the release later moves to `main`, update the image URLs only after the assets exist there.

Standalone WizardEx and RelicEx are deprecated: their content is included in PlayerEx 5.0.0. Keep that notice in the project descriptions and release notes so users know to remove the old addon JARs when setting up this port.
