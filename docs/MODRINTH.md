<p align="center">
  <img src="https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/branding/playerex-dc-banner.png" alt="PlayerEx DC" width="900">
</p>

<p align="center">
  <a href="https://github.com/BareMinimumStudios/playerex/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-BML_1.0-F2D36B?style=for-the-badge&amp;labelColor=292A27" alt="License: BML 1.0"></a>
  <a href="https://github.com/BareMinimumStudios/playerex/stargazers"><img src="https://img.shields.io/github/stars/BareMinimumStudios/playerex?style=for-the-badge&amp;labelColor=292A27&amp;color=F2D36B" alt="GitHub stars"></a>
  <a href="https://github.com/BareMinimumStudios/playerex/network/members"><img src="https://img.shields.io/github/forks/BareMinimumStudios/playerex?style=for-the-badge&amp;labelColor=292A27&amp;color=F2D36B" alt="GitHub forks"></a>
  <a href="https://github.com/BareMinimumStudios/playerex/issues"><img src="https://img.shields.io/github/issues/BareMinimumStudios/playerex?style=for-the-badge&amp;labelColor=292A27&amp;color=F2D36B" alt="Report an issue"></a>
</p>

<p align="center">
  <a href="https://github.com/BareMinimumStudios/playerex#how-it-works"><img src="https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/branding/button-guide.png" alt="Read the guide" width="224" height="64"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/playerex-dc"><img src="https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/branding/button-curseforge.png" alt="Download on CurseForge" width="224" height="64"></a>
  <a href="https://modrinth.com/mod/playerex-dc"><img src="https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/branding/button-modrinth.png" alt="Download on Modrinth" width="224" height="64"></a>
</p>

<p align="center">
  <strong>Minecraft 1.21.1 · Fabric + NeoForge · Java 21 · Client &amp; Server</strong><br>
  <a href="https://github.com/BareMinimumStudios/playerex">Source code</a> · <a href="https://discord.gg/pcRw79hwey">Discord community</a>
</p>

# PlayerEx

Your character should grow with the time you spend playing. PlayerEx brings RPG progression to Minecraft: earn levels, choose where your skill points go, and keep improving the equipment you take into a fight.

This **Minecraft 1.21.1** version runs on **Fabric and NeoForge**. It carries forward PlayerEx: Director's Cut, with **WizardEx and RelicEx content built in**, a new character screen, and support for the magic and accessory mods you choose to install.

**WizardEx and RelicEx are deprecated as standalone addons.** Their features are now built into PlayerEx 5.0.0. Do not install the old addon JARs alongside this release.

**PlayerEx 5.0.0 is the Minecraft 1.21.1 release.** Use matching PlayerEx versions on the server and clients, and keep backups when updating an existing world.

## How it works

Open the character screen with **P**, or click its tab beside your inventory. Collect vanilla experience, then spend your XP levels to raise your separate **PlayerEx level**. Each level gives **one skill point by default**, which you can put into your attributes. Level costs and the number of points awarded are configurable.

### Open PlayerEx from your inventory

Click the gold button beside your inventory to open the character screen. Hover over it to see the PlayerEx tooltip, or press **P** to open the screen directly.

[![The PlayerEx inventory button and its hover tooltip](https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/images/playerex-inventory-button.png)](https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/images/playerex-inventory-button.png)

You decide how to build your character. Put points into Constitution if you want more health and armor, lean into Strength for melee damage, or develop Intelligence and individual spell schools for a magic-focused setup. You can mix attributes freely; there is no class selection that locks you into one path.

Changing your mind is part of the process. Refund points let you take owned allocations back and spend those skill points elsewhere. Refund points are separate from skill points, so rebuilding a character has its own cost.

## Six attributes, plenty of room to experiment

The default relationships give each primary attribute a different role:

| Attribute | What it helps with |
| --- | --- |
| **Constitution** | Maximum health, armor, oxygen bonuses and poison resistance. With compatible magic attributes installed, it also contributes to magic resistance. |
| **Strength** | Melee attack damage, melee critical damage, knockback resistance and block-breaking speed. |
| **Dexterity** | Attack speed and ranged critical damage, plus ranged damage and haste when Ranged Weapon API is installed. |
| **Intelligence** | Experience dropped by defeated entities, Wither resistance and compatible spell haste, critical damage and school power. |
| **Luckiness** | Luck, evasion and critical chance for melee, ranged and compatible spell combat. |
| **Focus** | Passive health regeneration, healing amplification, and Fire, Freeze and Lightning resistance. |

These are the supplied defaults. **Data Attributes** lets pack authors change the relationships, limits and scaling. Some bonuses use diminishing returns, so a percentage stat may grow more slowly as you invest more points.

Focus regeneration is passive, but it starts small. With the default relationships, one Focus point gives **0.015 health per second**. It is gradual recovery, rather than an instant replacement for food or healing items.

## A character screen that shows the whole build

The screen has three pages:

- **Attributes:** spend and refund points, check regeneration and resistances, and see your current health and oxygen.
- **Combat Stats:** inspect melee and ranged bonuses, defenses, reach, breaking speed and other useful values.
- **Spell Schools:** invest in available schools and check spell power, spell criticals and, on supported Fabric setups, mana.

The pages use the supplied stone frame, gold icons, ivy and themed resource bars. Tooltips explain the values, while the bottom of the screen keeps your level and next-level XP cost close to the leveling controls.

### Attributes

Your main character page: choose your attributes, manage skill points and check health, oxygen and resistances.

[![PlayerEx Attributes page with allocation controls, vitality, resistances and resource bars](https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/images/playerex-attributes.png)](https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/images/playerex-attributes.png)

| Combat Stats | Spell Schools |
| :---: | :---: |
| [![PlayerEx Combat Stats page showing melee, ranged, defense and utility values](https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/images/playerex-combat-stats.png)](https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/images/playerex-combat-stats.png) | [![PlayerEx Spell Schools page showing ten magic schools and spell critical values](https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/images/playerex-spell-schools.png)](https://raw.githubusercontent.com/BareMinimumStudios/playerex/1.21.1/docs/images/playerex-spell-schools.png) |
| Check your combat bonuses and defenses. | Allocate school points and inspect available spell power. |

Click a screenshot to view it at full size.


## Equipment grows with you

Weapons and equipped armor can gain experience from kills and improve as they level. Weapons gain damage bonuses; armor gains armor and configurable damage reduction. Their leveling rules are separate, so a pack can change or disable one without changing the other.

Item tooltips show the equipment's level and progress. You can choose a compact display, expand details with Shift, and adjust tooltip colors in the client settings. Relic rarity colors help you tell different tiers apart at a glance.

Eligible items can survive reaching zero durability as **broken equipment**. A broken item shows an **X** in the inventory and stops providing its normal combat benefits until repaired. The number of times an item can survive breaking, infinite preservation and break notifications are configurable. Eligibility is controlled by item tags; preservation is not automatically applied to every item from every mod.

## Relics and useful finds

RelicEx content is included in PlayerEx. There is no separate RelicEx addon to install for this version.

Find **rings, amulets, head relics and chest relics** with randomly rolled attribute bonuses. Relics have **seven rarity tiers** and can carry **one to five distinct attributes**. Their rolls stay with the item, so an existing relic does not reroll when you equip it again or change the loot settings.

Rings and amulets use **Trinkets on Fabric** or **Curios on NeoForge**. Head and chest relics can also transfer their bonuses to matching armor in an anvil. Infusion costs no XP, consumes the relic and preserves the target armor's existing components. Armor that is already infused cannot receive another infusion. Relic armor uses native rendering with armor-trim support.

The loot pool also includes:

- **Tomes:** grant a PlayerEx level without spending vanilla XP.
- **Lesser and Greater Orbs of Regret:** add refund points so you can reconsider your allocations.
- **Dragon Stones:** reset progression after a confirmation use.
- **Relic Shards:** turn unwanted relic equipment into a source of experience through smelting and shard use.
- **Small, Medium and Large Health Potions:** automatically heal from your inventory, restoring 4, 6 or 8 health respectively. They are consumed automatically, even at full health, so keep them stored until you want to use them.

Chest loot, mob drops, rarity weights and dimension rules are configurable. Pack authors can control how often these items appear and where players are likely to find them.

## Magic, with room for more schools

WizardEx progression is also built in. Add **Spell Power Attributes** and compatible spell content to connect your character's development to magic.

PlayerEx supports **Fire, Frost, Arcane, Healing, Lightning and Soul**, plus **Water, Earth, Air and Nature** through **More RPG Library**. Available schools have their own allocation controls and icons. Intelligence contributes to available school power, while other attributes help shape spell criticals and related bonuses.

Only schools supplied by your installed mods become available. PlayerEx supplies progression and compatibility; the libraries and spell-content mods provide their spells, casting rules and external attributes. If you remove an optional school provider, saved allocations remain available for refunds.

### Mana and rune substitution on Fabric

With **Spell Engine** and **Mana Attributes** installed on Fabric, eligible spells can use mana when you do not have enough of their required consumable runes. Existing runes are preferred, and eligible More RPG rune stones are supported too. Ordinary reagents and arrows are not replaced with mana.

Mana Attributes supplies the mana pool, regeneration and its HUD. PlayerEx handles the connection to eligible Spell Engine costs. The **minimum automatic mana cost is 20**, with stronger or repeated spell launches costing more. Set enough mana capacity through your gear or configuration: installing the integration alone does not guarantee that a fresh character can afford a cast.

**Mana integration is Fabric-only.** The NeoForge version still supports compatible spell attributes and school progression, but does not include this mana backend.

## Installation

Use **Minecraft 1.21.1** and **Java 21**, then install the PlayerEx file for your loader on the server and each client.

Required dependencies:

- **Both loaders:** [Data Attributes](https://modrinth.com/mod/dataattributes) **3.0.0 or newer** and Fzzy Config. The build targets Fzzy Config **0.7.7** for the appropriate loader.
- **Fabric:** Fabric API **0.116.15 or newer** and Fabric Language Kotlin **1.13.7+kotlin.2.2.21 or newer**.
- **NeoForge:** KotlinForForge **5.11.0 or newer**.

**[Remnant 3.0.0](https://modrinth.com/mod/remnant) is required and already bundled inside PlayerEx as a separate nested JAR.** It does not need a separate download. Crunch is bundled too.

Optional integrations depend on the features you want:

- [Spell Power Attributes](https://modrinth.com/mod/spell-power): magic attributes and school progression.
- [More RPG Library](https://modrinth.com/mod/more-rpg-library): additional schools and RPG attributes.
- [Spell Engine](https://modrinth.com/mod/spell-engine): spell integration and eligible Fabric rune substitution.
- [Mana Attributes](https://modrinth.com/mod/mana-attributes): the Fabric mana system.
- [Trinkets](https://modrinth.com/mod/trinkets) or [Curios](https://modrinth.com/project/vvuO3ImH): ring and amulet slots on Fabric or NeoForge respectively.
- [Critical Strike](https://modrinth.com/mod/critical-strike): compatible critical-hit handling.
- [Ranged Weapon API](https://modrinth.com/mod/ranged-weapon-api): ranged attributes.

Install the 1.21.1 versions and their own required dependencies. This port supports Fabric and NeoForge directly; legacy Forge and Quilt are not listed as supported loaders for this build.

## Make it fit your pack

Configure player and equipment level costs, skill points per level, death resets, item break behavior, loot chances and more. Data Attributes controls attribute relationships and scaling; PlayerEx's grouped configuration handles its gameplay settings.

Gameplay rules belong to the server. Tooltip presentation, sound volumes and other display preferences belong to the client. Operators can use commands to inspect or adjust progression, including:

```text
/playerex level get <player>
/playerex level add <player> [amount]
/playerex skill <id> add <player> [amount]
/playerex refund add <player> [amount]
/playerex reset <player> [retain]
```

These commands require **operator permission level 2**. Administrative level and skill grants do not charge the target's XP or skill points. The optional reset value is the percentage of progression to retain.

## Coming from 1.20.1?

The 1.21.1 port uses a new save system. **Old 1.20.1 PlayerEx progression, configuration files and legacy RelicEx item data are not automatically imported.** Plan a fresh setup or your own migration rather than expecting an old world to carry those values across unchanged.

Earlier 1.21.1 development saves use the current attachment and item-component formats. Read the version's changelog before updating, and use matching mod files on both sides of a multiplayer connection.

## Credits and support

PlayerEx builds on **CleverNucleus's original PlayerEx** and the work of the **PlayerEx: Director's Cut contributors**. This version also carries forward WizardEx and RelicEx content. Thanks to the original authors, translators, artists and everyone helping test the port.

Found a problem? Include your PlayerEx version, loader, relevant mod list and latest log in an issue. A clear screenshot or short recording helps with interface and rendering bugs.

PlayerEx uses **BML-1.0**. See the [license](https://github.com/BareMinimumStudios/playerex/blob/main/LICENSE) and the preserved third-party license files in the source repository.
