# Changelog

Changes that affect players, pack authors and developers are collected here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [5.0.1] - 2026-10-07

### Added

- Loader-specific optional integrations in Modrinth and CurseForge publishing metadata.
- Inventory-button screenshot in the README and marketplace descriptions.
- Character-screen screenshots in the README and marketplace descriptions, with Attributes as the main image and Combat Stats and Spell Schools below it.

### Changed

- Consistent guide/download buttons and PlayerEx DC branding, with the original book style, a transparent banner and a shared 256×256 game/social icon.

### Fixed

- Chopped character-screen crest decoration caused by splitting its stonework across the expanded header.
- NeoForge client startup failure caused by static event handlers on Kotlin object subscribers.
- Fresh GitHub builds failing while preparing NeoForge's patched Minecraft files. Disable Gradle configuration caching and parallel project execution for the current Cloche toolchain.

## [5.0.0] - 2026-10-07

The first Minecraft 1.21.1 release, available for Fabric and NeoForge. This brings the work from the development builds into one release.

### Added

- Built-in WizardEx spell-school progression and RelicEx loot, relic equipment, infusion and consumable items.
- A character screen with Attributes, Combat Stats and Spell Schools pages, inventory access, school icons and the supplied stone, ivy and resource-bar artwork.
- More RPG Library support for Water, Earth, Air and Nature alongside the core spell schools.
- Fabric-only Spell Engine integration with Mana Attributes: mana costs, regeneration through the mana backend and substitution for missing eligible runes. The minimum automatic mana cost remains 20.
- Native relic armor rendering with support for armor trims.
- Separate publishing targets for each loader and platform, using mc-publish with verified artifacts and release-specific notes.

### Changed

- Use the published Data Attributes 3.0.0 and Remnant 3.0.0 artifacts. Remnant remains a required separate mod bundled as a nested JAR; it does not need another download.
- Require Fabric Language Kotlin 1.13.7+kotlin.2.2.21+ or KotlinForForge 5.11.0+ to match the public dependencies.
- Store progression in loader-native attachments and item components. Existing 1.21.1 development saves keep their schemas; 1.20.1 progression and legacy item data are not automatically imported.
- Group configuration settings by purpose while retaining the existing 1.21.1 saved keys.
- Keep equipment tooltips compact, remove duplicate bonus rows and apply rarity colors consistently.
- Update download buttons to the PlayerEx DC project pages and document installation, commands and integrations.

### Deprecated

- Standalone WizardEx and RelicEx addons. Their features are now built into PlayerEx; do not install their old JARs alongside PlayerEx 5.0.0.

### Fixed

- NeoForge configuration loading racing with other mods during parallel initialization. Load PlayerEx settings in queued common setup so existing values are read reliably.

- Language-file rejection that caused missing labels and broken tooltips. The build now rejects non-string translations before packaging.
- The mob-kill crash when the defeated entity has no Luck attribute.
- Relic armor cube rotations that left model parts misaligned.
- Saved health being clamped to 20 before login attribute reconciliation.
- Multi-level purchase costs, reset retention, refund bounds and the original operator command structure.
- Inventory-button hover borders, header placement and XP-label alignment.

### Removed

- The private local Remnant Maven repository, development logs, temporary probes and obsolete milestone notes from the release source package.

[Unreleased]: https://github.com/BareMinimumStudios/playerex/compare/v5.0.0...HEAD
[5.0.0]: https://github.com/BareMinimumStudios/playerex/releases/tag/v5.0.0
