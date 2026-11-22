# Compatibility

Minecraft 1.21.1; Java 21; Fabric and NeoForge. Common code owns gameplay, attachment schemas and server-authoritative transactions. Loader adapters own attachment storage, registration and networking. Clients and servers should use matching PlayerEx builds.

## Required dependencies

Public Data Attributes 3.0.0 or newer supplies attribute semantics and relationships. Fzzy Config supplies configuration. Fabric needs Fabric API 0.116.15+ and Fabric Language Kotlin 1.13.7+kotlin.2.2.21+; NeoForge needs KotlinForForge 5.11.0+. Exact loader/build versions are declared in `libraries.toml` and `build.gradle.kts`.

**Remnant 3.0.0 is required and bundled as a separate nested mod on each loader.** It owns its mod ID, entrypoints and saved offline ledger. PlayerEx registers `playerex:player` directly. Loader-native PlayerEx attachments remain canonical live progression state. The public loader-specific JAR is downloaded from Modrinth Maven and nested without modifying its bytes. Its upstream license is retained in `META-INF/licenses/Remnant-LICENSE`; source is available at https://github.com/BareMinimumStudios/remnant. No separate installation is needed. Crunch is also nested.

WizardEx and RelicEx are deprecated as standalone addons because their content is built into PlayerEx 5.0.0. The old addon JARs are not compatible with this port.

## Optional integrations

- Iron's Spells 'n Spellbooks 1.21.1-3.16.4 or newer: optional NeoForge-only native school progression, configurable attribute bonuses and server-side spell-level requirements. The API is compile-only; install Iron's and its own dependencies separately.
- Spell Power: external power/critical attributes and built-in WizardEx school progression.
- More RPG Library: additional Water, Earth, Air and Nature school attributes, alongside core Fire, Frost, Arcane, Healing, Lightning and Soul.
- Spell Engine: spell integration. Fabric mana substitution was verified against Spell Engine 1.10.9, Spell Power 1.6.0 and More RPG Library 2.7.2.
- Mana Attributes 2.9.1: Fabric-only mana capacity, regeneration, persistence and native networking/HUD. PlayerEx substitutes mana for missing consumable runes and eligible More RPG stones; ordinary reagents/arrows are not substituted. Minimum automatic cost is 20; external default capacity 10 is unchanged. Rune, infinity, creative and native exemptions take precedence. Mana-only spells without consumable rune costs are unchanged. No mana debt or spending damage bonus is added.
- Trinkets on Fabric / Curios on NeoForge: relic ring and amulet slots. Accessories is not used.
- Critical Strike: owns critical-hit execution when installed, avoiding duplicate PlayerEx critical rolls.
- Ranged Weapon API: external ranged attributes; absent values remain unavailable in the GUI.

Missing optional schools reject new allocations but retain saved owned points for refunds. Optional mod classloading remains isolated. Native relic armor rendering supports armor trims and does not require AzureLib, GeckoLib or Armor Model Lib.

Old binary addons and old owo UI extensions require a source port. No live CCA, Endec, owo networking, Additional Entity Attributes or Offline Player Cache dependency is restored.

See [feature audit](FEATURE_PARITY.md) for tested behavior and connected-client/lifecycle limitations.

## Configurable magic and dimension progression (5.1.0)

NeoForge's `config/playerex/magic.json5` is server-synced through Fzzy Config, including mode, per-school thresholds, casting exemptions and per-point native attribute bonus maps. Values for percentage-style Iron's attributes use 1.0 = 100%; maximum mana uses whole mana units. Defaults add 2 maximum mana, 0.5 percentage points of general spell power and summon damage, and 0.25 percentage points of cast-time reduction per effective Intelligence point; Focus adds 1 percentage point of mana regeneration and 0.25 percentage points of cooldown reduction. Each owned school point adds 1 percentage point of that school's power. Targets must be registered `irons_spellbooks:*` attribute IDs. Changing, removing or disabling rules removes obsolete PlayerEx modifiers on the next one-second update. PlayerEx modifiers are transient and use stable IDs, so reconnects do not stack bonuses. Native reductions may have additional diminishing returns inside Iron's.

Spell-level restrictions default off; scroll restrictions default off and creative bypass defaults on. If enabled, checks run on the server's cancellable pre-cast event for spellbooks, swords and optionally scrolls. Command/non-player casts and unrecognized addon schools are left alone. Owned allocation counts determine access independently of gear. A requirements list entry corresponds to each spell level, and higher levels than the configured list remain locked. Invalid lists fall back to the default. Nine built-in Iron's schools are supported; adding a completely new allocation school requires a registration change. Existing RPG Series relationships continue to use Data Attributes.

Both loaders expose `dimensionCapsEnabled`, `startingDimensionCap` and `dimensionLevelCaps` in the existing server config. Rules are re-evaluated from saved visits, bounded by the global level attribute maximum. Visits survive death, reset stones and progression resets. Current dimensions are recorded on login and during play even while caps are disabled. Historical visits from before the update cannot be reconstructed. The feature does not grant levels or XP, alter the Nether portal, or lower existing levels.

## Pufferfish's Skills on NeoForge

PlayerEx 5.1.0 registers its base attributes in NeoForge's default player attribute supplier. This lets Pufferfish's Skills validate rewards such as `playerex:strength` without a dedicated adapter or an additional required dependency. Data Attributes still controls configured values, bounds and derived relationships. PlayerEx reconciliation changes only its own modifiers, retaining bonuses awarded by other mods; external bonuses do not become owned allocation points.
