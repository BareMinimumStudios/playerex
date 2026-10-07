# PlayerEx 1.21.1 feature audit — 5.0.0

Audited against the supplied local PlayerEx, WizardEx and RelicEx 1.20.1 source, not descriptions alone. This is a behavior port with new loader-native storage/networking and a new GUI; it is not binary compatibility with old addons. A compile or dedicated-server probe does not verify client rendering or a connected-player lifecycle.

## Original PlayerEx

| Area | Current implementation | Evidence / qualification |
| --- | --- | --- |
| Six primary attributes, level, skill/refund points | Server-owned allocation map; deterministic transient modifiers; validated purchase/allocation/refund requests | Shared progression tests and earlier both-loader gameplay checks reported by the user; school/utility probes use the same request handlers |
| Level formula and configurable points per level | Crunch formula compilation; previous valid formula retained on invalid updates | Math tests; deliberate batch-cost correction described below |
| XP negation and restorative force | Accepted-orb reduction, lazy game-time recovery; original 95%, 600 ticks and 110% defaults | Earlier both-loader gameplay checks; no ticking chunk component |
| Death reset | Boolean resetOnDeath, default false; native attachment copy and explicit reconciliation | Preserved original Boolean choice, not a percentage setting; full connected death/restart matrix still needs a final pass |
| Passive health regeneration | Once per second by default; restored ON_TICK option | Both-loader fixture verifies timing; Focus 1 gives 0.015 hp/s with default relationships, intentionally slow |
| Heal amplification, elemental/status resistance, evasion, lifesteal | Shared server hooks; inverted-healing undead Wither/potion exceptions restored | Math/gameplay fixtures; lifesteal uses actual accepted health loss |
| Melee/ranged criticals | Legacy fallback; relinquishes execution to optional Critical Strike | Existing compatibility policy retained; every external combat mod combination is not runtime-tested |
| Breaking speed, oxygen, dropped XP | Shared gameplay hooks; AEA replacements use native oxygen_bonus and PlayerEx dropped-experience attribute | Replacement semantics differ from legacy AEA; no AEA owned dependency |
| Weapon and armor leveling | Separate enable/max/formula/bonus settings; separate passive/hostile/boss XP overrides | Both-loader fixture verifies sword XP 1 versus helmet XP 10; -1 overrides inherit existing shared item XP settings |
| Armor damage reduction | Four equipped pieces contribute capped configured reduction | Shared implementation; earlier tests, no claim of all modded armor compatibility |
| Broken-item preservation and repair | Original inventory X, numbered red/bold broken line, anvil repair hint, blocked startUsingItem; vanilla data component; configured break count/infinite mode; broken attribute suppression and repair clearing | Prior both-loader item persistence checks; Binding destruction setting now tested on both |
| Break messages | Restored configurable server messages | Compiled; actual connected chat display pending |
| Nameplate level/color | Restored client settings, original <Lv. n> label | Exact 1.21.1 renderer signature verified; live render pending |
| Three equipment-tooltip modes | Default delegates to native; Vanilla shows base weapon modifier delta; PlayerEX includes current player value without counting the same equipped modifier twice | Client-only native addModifierTooltip hook; progression bonuses combine with existing additive rows for display only. Both-loader fixture verifies 6.3 damage in one sword row while authoritative rows remain separate; live client injection execution pending |
| Progression/refund audio and volumes | Original sounds, per-notification local 0–100 volume, refund pitch 0.7 | Compiled/resource checked; audible verification pending |
| GUI disable and inventory access | Server disableUI respected at open/tick/inventory tab; P key and normal/creative inventory entry | New requested native asset-based GUI; original minus key/UI extension registry not copied |
| Damage/refund extension APIs | Ordered registered predicates/functions; modern PlayerExState refund conditions; invalid numeric results ignored | Both-loader fixture checks zero-damage callback and extra refund capacity |
| Public item tags and trade attribute IDs | Four public tag keys; seven trade IDs registered | Registry fixture passes; original code has no automatic trade-skill XP system |
| Remnant | Required standalone jar-in-jar offline ledger | Both-loader registration, cache lookup and native NBT save/load verified; canonical live state remains the PlayerEx attachment |

Original xpFromMiniboss settings were unused in the supplied kill-XP code. No invented miniboss classifier is added. Fzzy replaces the original nested owo configuration; values have modern names and bounds, not an old config-file importer. Client preferences are local in playerex:client_sounds; gameplay config remains server-owned.

## Built-in WizardEx and requested extensions

| Area | Current implementation | Evidence / qualification |
| --- | --- | --- |
| School allocations | Fire, Frost, Arcane, Healing, Lightning, Soul, Water, Earth, Air, Nature; shared points and refunds | All ten registered external targets and actual +0.5 allocation effects pass on both loaders with Spell Power and More RPG Library |
| Intelligence school contribution | +0.25 per Intelligence to each available school | Data Attributes relationships; Soul and four More RPG schools are requested extensions |
| Wizard critical chain | Luckiness -> diminished chance factor -> Spell Power chance; Dexterity -> diminished damage factor -> Spell Power damage | Original local-source coefficients restored; core Intelligence haste/crit contribution retained |
| Optional libraries removed | Missing schools cannot receive new allocations; saved owned allocations can be refunded | Availability/refund fixtures; no optional hard class links in shared logic |
| GUI | School controls plus power/crit readings; ten distinct packaged emoticon glyphs in allocation/power rows; native mana readout on Fabric when available | Display uses Spell Power percent baselines; actual client layouts pending |

Supplied WizardEx GUI exposes four schools; its data also includes Lightning. Cross-school penalties claimed in a published description are not implemented by that local source, so they are not invented here.

## Built-in RelicEx

| Area | Current implementation | Evidence / qualification |
| --- | --- | --- |
| Ring, amulet, head and chest relics | One server roll; seven rarities; one to five distinct weighted attributes; persistent/networked component | Both-loader save/network/no-reroll fixtures; deterministic roll math tests |
| Trinkets / Curios | Optional Fabric Trinkets 3.10.0 and NeoForge Curios 9.5.1 slot adapters | Real registration and native getModifiers calls pass in both fixtures; connected equip/swap timing pending |
| Armor infusion | Matching-slot anvil transfer, zero XP, preserve target components, consume inputs, reject repeats | Actual AnvilMenu transactions pass on both loaders |
| Crafting repair safeguards | Reject repair recipe for rolled relics/infused armor so rolls cannot disappear | Native recipe matches/assemble pass on both |
| Chest and mob loot | Configured independent chest chances; Enemy mob weighted selection, luck, player-only/blacklist; dragon stone special drop | Actual chest injection and mob restriction/selection fixtures pass; exhaustive statistical distributions and dragon death pending |
| Dimension rules | Default Overworld/Nether/End rules; configurable custom dimensions; bounded rolled-rarity acceptance for mobs | Map config loads and zero-acceptance test passes; chest ignores mob-only rules |
| Shard smelting | All four relic equipment types -> shard, 200 ticks, 0.7 XP | Actual loaded recipe matches and assembly pass both loaders; full furnace tick not simulated |
| Tome, two regret orbs, dragon stone | Free level/point, capped refunds, per-stone confirmation then reset | Both-loader utility/component fixtures |
| Three health potions | Auto-use on inventory tick, heal 4/6/8, original consume-at-full-health/creative behavior | Small potion fixture passes; native sound resource restored |
| Item visuals and rarity tint | Original item textures/models; both-loader rareness predicate; original seven names/colors | Packaged resources; live item visual check pending |
| Armor geometry and trims | Original seven geometry files/textures converted to cached native HumanoidModel; native armor, glint and trim rendering | Actual Minecraft model-class test checks finite poses/cubes and retains all 14/11/13/21/19/12/16 source cubes; live rendering and projected trim appearance pending |

Relic attribute IDs remain saved when an optional attribute disappears, but cannot contribute modifiers until registered again. Reloadable weight resources live under data/playerex/playerex/relic_weights. Native model caching clears on resource reload; no animation/model library is required.

## Fabric-only mana: accepted scope

Mana Attributes 2.9.1 owns current/max mana, regeneration, persistence and its native networking/HUD. PlayerEx optionally bridges Spell Engine 1.10.9 consumable rune costs. Rune inventory/infinity/creative/native cost exemptions remain preferred; shortages of runes: items/tags or the four More RPG aqua/terra/storm/nature stones may use mana. Other reagents and arrows are not substituted.

Automatic cost = 20 * max(1, total damage/healing coefficient) * max(1, 1 + extra launch count) * required rune amount. The user explicitly keeps minimum 20. Mana Attributes' default maximum 10 is unchanged; gear/config must raise capacity. Finite explicit cost.rpgmana values 0..100000 override the automatic rule; -1 selects automatic. Mana-only spells without a consumable rune requirement are not changed.

Server reserves mana before delayed delivery, refunds failure once, and shares reservations for batching/channel processes. Fixture checks successful, failed and delayed delivery, repeated callback, insufficient mana, explicit and invalid cost, rune priority, More RPG stones, arrow exclusion, batching and the native failed-delivery hook. Full connected cast/channel, interrupted/disconnected pending delivery and restart timing remain pending. No NeoForge mana backend, debt, spending damage bonus, extra mana potions or enchantments are requested.

## Deliberate corrections and modern boundaries

- Multi-level purchases charge the sum of individual next-level costs instead of the old target-cost-times-count bug.
- Lifesteal rewards actual health lost after accepted damage; evaded/blocked damage does not heal the attacker.
- Login health restores saved vanilla Health once after allocation maximum health is reconstructed, preserving full/wounded/zero health instead of early clamping to 20.
- Item leveling enable settings are independent from the original accidental item-breaking gate.
- Tome can reach the actual configured maximum; Greater Orb considers all owned schools including Lightning.
- Trade ID collection includes Smithing and does not duplicate Enchanting.
- Mob dimension rarity preferences use the rolled rarity; the old item-ID attribute-weight lookup bug is not reproduced. Sampling is bounded to 32 attempts, then no relic is emitted.
- Armor repair safeguards also cover infused ordinary armor. Native trims are a new requested feature.
- New loader attachments, ResourceLocation modifier IDs, typed packets and vanilla item components replace CCA/Endec/owo/AEA. Protocol remains 3; matching client/server registrations are required.
- No legacy 1.20.1 player/item NBT import. Old CCA API consumers, UUID modifiers, owo screen extensions and addon binaries need an actual source port; registry hooks restored here are modern APIs.

## Release verification still required

Client visual/input/audio tests on both loaders, native relic armor and trim appearance, connected login/equipment health ordering, complete mana channel/interrupt lifecycle, and modded inventory conflicts remain open. These do not negate passing compilation or dedicated-server fixtures, but prevent claiming every feature is 1:1 verified.


## Alpha.21 tooltip and subtle-feature recheck

User requested the presentation of Lukas' Weapon Leveling architectury-1.21.1. Source inspected: https://github.com/GekidoLukas/weaponleveling/tree/architectury-1.21.1 (ClientEvents, TooltipHelper, client config). PlayerEx has an independent implementation of the observed layout; no dependency or wholesale source/asset import from that mod. This presentation request does not replace PlayerEx kill XP/formulas, add Weapon Leveling hit-XP/broken-item wrapper mechanics, or claim compatibility with its API.

Item Level section is inserted after the item name; arrow Level/Progress rows, the observed default RGB palette, two-decimal percent normal progress, raw current/required XP with advanced tooltips, max/above-configured-max states, optional Shift expansion and local configurable colors. Armor retains its capped per-piece reduction line. Hidden tooltips and disabled item leveling suppress the section. Original PlayerEx's numbered bold red Broken line and owned broken.png X are restored; repair instruction is added. The X uses the original 8x8 top-right placement and clears when the existing repaired-state rule clears broken. Original LivingEntity.startUsingItem cancellation was missing and is restored in addition to existing attack/interact/mine/use guards.

Both-loader dedicated probes pass actual hurtAndBreak preservation, break count, blocked startUsingItem, repair clearing, tooltip ordering/style, normal percentage, advanced XP, collapsed hint, cap state, armor reduction and hidden tooltip. A separate presentation helper test merges level bonuses into existing additive rows without mutating authoritative modifiers. Original event/sound/GUI APIs that depended on Fabric events, CCA and owo remain modernized boundaries rather than binary-compatible copies. Regeneration uses exactly 20 ticks for once-per-second; the old private counter ran its event after 21 ticks. Existing deliberate bug corrections and unavailable legacy save/API imports above remain explicit exceptions to literal 1:1.

Ten new packaged 9x9 pixel glyphs represent Fire, Frost, Arcane, Healing, Lightning, Soul, Water, Earth, Air and Nature. The font is isolated at playerex:schools/private-use glyphs, so it does not replace default text or rely on unsupported emoji. Allocation and power-row text reserves space for the icon; hitboxes and numeric columns retain their existing bounds. Atlas preview inspected; live client render/placement still needs checking.


### Alpha.22 corrections

Derived tooltip modifiers now use one canonical equipment group; native slot gameplay is unchanged. Seven rarity tiers color both name and label; zero armor reduction is hidden. Fzzy UI groups preserve saved keys, and stock palette migration preserves custom colors. Native cube rotations now match the original AzureLib Armor 2.0.14 pose conversion, verified for all seven armor/trim models. Live client visual parity remains unverified.


### Alpha.25 Remnant correction

Required loader-specific Remnant 2.0.0-beta.5 is nested as a standalone mod. Direct PlayerLedger registration, offline lookup and native NBT ledger save/load pass on both loaders with no external Remnant JAR. Existing live PlayerEx state/schema remain unchanged.

## Alpha26 commands and cleanup

Original operator command tree, target selectors/names, free administrative grants, capped refunds, held-item level controls and retained resets are restored. Trade allocations reconcile through the existing state schema; normal GUI/network requests retain their original validation and costs. Integer retention and item-component bounds are documented in COMMANDS.md. Historical development logs and notes are archived outside the source checkout. See CHANGELOG.md for validation and remaining limits.

## Public dependency release verification — 2026-10-07

The release now resolves Data Attributes 3.0.0 and Remnant 3.0.0 from their public Modrinth Maven artifacts, pinned separately for each loader. Remnant remains a required standalone nested mod, with exact-byte checks against the published files. Its license is retained separately in PlayerEx's resources.

Both-loader dedicated-server probes passed with the public dependencies: vanilla language loading, the Data Attributes presentation API and Focus 1 giving 0.015 hp/s, Remnant registration/cache/offline lookup/NBT save-load, commands and permissions, equipment/relic behavior, retained non-default configuration values and the missing-Luck regression. NeoForge config loading is queued during common setup to avoid concurrent use of Fzzy's shared JSON5 parser. These tests use disposable fixtures; they do not replace connected-client rendering or lifecycle checks.

Standalone WizardEx and RelicEx are deprecated because their features are built into PlayerEx 5.0.0. Their legacy addon binaries are not required or supported by this port.
