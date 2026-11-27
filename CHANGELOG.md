# Changelog

Changes that affect players, pack authors and developers are collected here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and version numbers follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [5.1.1] - 2026-11-27

### Changed

- Level-cap checks skip dimension history when caps are disabled and stop scanning once the global cap is unlocked.
- Build and publishing workflows validate the same dated changelog entry. All three publishing destinations use that entry for release notes.

## [5.1.0] - 2026-11-21

### Added

- Optional NeoForge-only Iron's Spells compatibility, with nine separately allocated schools and configurable native Intelligence, Focus and school-power bonuses.
- Separate RPG Series and Iron's school pages. Only available systems get a magic tab; Attributes remains the default page.
- Optional server-enforced Iron's spell-level requirements, per-school point thresholds, spell exemptions, scroll handling and creative bypass. Restrictions default off.
- Configurable dimension-unlocked level caps on both loaders. A visited dimension can unlock a higher cap permanently, with in-game editing, modded dimension IDs and a global-cap unlock option.

### Changed

- Player state saves and Remnant snapshots retain visited dimensions using an optional field that loads older saves without a reset. The NeoForge network protocol is now 4; clients and servers must update together.
- NeoForge publishing metadata lists Iron's Spells as optional. Iron's and its dependencies are not bundled.
- Updated the Gradle wrapper from 9.8.0 to 9.9.1, with refreshed launcher scripts and the official distribution checksum.

### Fixed

- Register PlayerEx base attributes in NeoForge's default player supplier so Pufferfish's Skills can validate attribute rewards ([#84](https://github.com/BareMinimumStudios/playerex/issues/84)).
- Avoid missing-school attribute warnings when an optional magic provider is not installed.
- Use loader-specific durability hooks so Fabric starts correctly and preserved broken items continue working across NeoForge durability layouts.
- Apply Fabric death resets after persistent attachments are copied, preventing initialization order from restoring pre-death progression.
- Match Iron's school page to the RPG page's panel size and reduce unused space.

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

[Unreleased]: https://github.com/BareMinimumStudios/playerex/compare/v5.1.1...1.21.1
[5.1.1]: https://github.com/BareMinimumStudios/playerex/compare/v5.1.0...v5.1.1
[5.1.0]: https://github.com/BareMinimumStudios/playerex/compare/v5.0.1...v5.1.0
[5.0.1]: https://github.com/BareMinimumStudios/playerex/compare/v5.0.0...v5.0.1
[5.0.0]: https://github.com/BareMinimumStudios/playerex/releases/tag/v5.0.0
