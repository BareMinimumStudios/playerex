package com.bibireden.playerex.config

import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.api.FileType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.event.api.ServerUpdateContext
import me.fzzyhmstrs.fzzy_config.util.Translatable
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString
import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.math.FormulaEngine

@RootConfig
@Version(1)
@Translatable.Name("PlayerEx")
@Translatable.Desc("Server-authoritative PlayerEx progression and compatibility configuration.")
class PlayerExConfig : Config(PlayerEX.id("config")) {
    @Translatable.Name("Player progression")
    val progressionGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("progression", false)
    @Translatable.Name("Skill points per level up")
    var skillPointsPerLevelUp: Int = 1
    @Translatable.Name("Level formula")
    @Translatable.Desc("XP levels charged for each PlayerEx level. Variable x is the next level. Invalid expressions retain the previous valid formula.")
    var levelFormula = ValidatedString(FormulaEngine.DEFAULT_LEVEL_FORMULA)
    @Translatable.Name("Reset progression on death")
    var resetOnDeath: Boolean = false
    @Translatable.Name("Disable PlayerEx menu")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var disableUI: Boolean = false

    @Translatable.Name("Weapon leveling")
    val weaponsGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("weapons", true)
    @Translatable.Name("Enable weapon leveling")
    var weaponLevelingEnabled: Boolean = true
    @Translatable.Name("Weapon max level")
    var weaponMaxLevel: Int = 500
    @Translatable.Name("Damage gained per level")
    var weaponDamagePerLevel: Double = 0.1
    @Translatable.Name("Weapon formula")
    @Translatable.Desc("Item XP needed for the next weapon level; x is the next level.")
    var weaponFormula = ValidatedString(FormulaEngine.DEFAULT_ITEM_FORMULA)
    @Translatable.Name("Passive mob XP")
    @Translatable.Desc("-1 inherits the shared kill reward in Advanced. Zero disables this reward; positive values set item XP per kill.")
    var weaponXpFromPassive: Int = -1
    @Translatable.Name("Hostile mob XP")
    @Translatable.Desc("-1 inherits the shared kill reward in Advanced. Zero disables this reward; positive values set item XP per kill.")
    var weaponXpFromHostile: Int = -1
    @Translatable.Name("Boss XP")
    @Translatable.Desc("-1 inherits the shared kill reward in Advanced. Zero disables this reward; positive values set item XP per kill.")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var weaponXpFromBoss: Int = -1

    @Translatable.Name("Armor leveling")
    val armorGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("armor", true)
    @Translatable.Name("Enable armor leveling")
    var armorLevelingEnabled: Boolean = true
    @Translatable.Name("Armor max level")
    var armorMaxLevel: Int = 500
    @Translatable.Name("Armor gained per level")
    var armorPerLevel: Double = 0.1
    @Translatable.Name("Damage reduction per level (%)")
    var armorReductionPerLevel: Double = 0.1
    @Translatable.Name("Maximum reduction per armor piece (%)")
    @Translatable.Desc("Each worn piece contributes one quarter of this capped value to total damage reduction.")
    var armorMaxReduction: Double = 25.0
    @Translatable.Name("Armor formula")
    @Translatable.Desc("Item XP needed for the next armor level; x is the next level.")
    var armorFormula = ValidatedString(FormulaEngine.DEFAULT_ITEM_FORMULA)
    @Translatable.Name("Passive mob XP")
    @Translatable.Desc("-1 inherits the shared kill reward in Advanced. Zero disables this reward; positive values set item XP per kill.")
    var armorXpFromPassive: Int = -1
    @Translatable.Name("Hostile mob XP")
    @Translatable.Desc("-1 inherits the shared kill reward in Advanced. Zero disables this reward; positive values set item XP per kill.")
    var armorXpFromHostile: Int = -1
    @Translatable.Name("Boss XP")
    @Translatable.Desc("-1 inherits the shared kill reward in Advanced. Zero disables this reward; positive values set item XP per kill.")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var armorXpFromBoss: Int = -1

    @Translatable.Name("Item breaking and repair")
    val breakageGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("breakage", true)
    @Translatable.Name("Preserve eligible broken items")
    @Translatable.Desc("Items in playerex:unbreakable_items remain unusable at zero durability until repaired, while allowed preserved breaks remain.")
    var itemBreakingEnabled: Boolean = true
    @Translatable.Name("Unlimited preserved breaks")
    var infiniteItemBreaking: Boolean = false
    @Translatable.Name("Preserved breaks before destruction")
    var timesItemCanBreak: Int = 3
    @Translatable.Name("Destroy broken Binding-cursed armor")
    var destroyCurseOfBinding: Boolean = true
    @Translatable.Name("Notify player when an item breaks")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var messageOnItemBreak: Boolean = true

    @Translatable.Name("Health regeneration")
    val regenerationGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("regeneration", true)
    @Translatable.Name("Regeneration interval")
    @Translatable.Desc("Once per second applies the regeneration attribute every 20 ticks. Every tick applies it 20 times as often.")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var healthRegenerationLifecycle = HealthRegenerationLifecycle.ON_EVERY_SECOND

    @Translatable.Name("Relics: chest loot")
    val chestLootGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("chestLoot", true)
    @Translatable.Name("Enable chest drops")
    var relicChestsHaveLoot: Boolean = true
    @Translatable.Name("Relic drop chance (%)")
    var relicChestChance: Int = 15
    @Translatable.Name("Lesser refund orb chance (%)")
    var relicChestLesserOrbChance: Int = 5
    @Translatable.Name("Greater refund orb chance (%)")
    var relicChestGreaterOrbChance: Int = 1
    @Translatable.Name("Tome chance (%)")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var relicChestTomeChance: Int = 5

    @Translatable.Name("Relics: mob loot")
    val mobLootGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("mobLoot", true)
    @Translatable.Name("Mob drop chance (%)")
    var relicMobLootChance: Int = 5
    @Translatable.Name("Require a player kill")
    var relicOnlyPlayerKills: Boolean = false
    @Translatable.Name("Dragon drops reset stone")
    var relicDragonDropsStone: Boolean = true
    @Translatable.Name("Excluded mob IDs")
    var relicMobBlacklist: List<String> = emptyList()
    @Translatable.Name("Relic relative weight")
    @Translatable.Desc("Relative share of a successful mob drop, not a percentage chance. Zero removes this outcome.")
    var relicMobRelicWeight: Int = 50
    @Translatable.Name("Potion relative weight")
    @Translatable.Desc("Relative share of a successful mob drop, not a percentage chance. Zero removes this outcome.")
    var relicMobPotionWeight: Int = 30
    @Translatable.Name("Lesser Orb relative weight")
    @Translatable.Desc("Relative share of a successful mob drop, not a percentage chance. Zero removes this outcome.")
    var relicMobLesserOrbWeight: Int = 5
    @Translatable.Name("Greater Orb relative weight")
    @Translatable.Desc("Relative share of a successful mob drop, not a percentage chance. Zero removes this outcome.")
    var relicMobGreaterOrbWeight: Int = 1
    @Translatable.Name("Tome relative weight")
    @Translatable.Desc("Relative share of a successful mob drop, not a percentage chance. Zero removes this outcome.")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var relicMobTomeWeight: Int = 10

    @Translatable.Name("Relics: dimension rarity rules")
    val dimensionLootGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("dimensionLoot", true)
    @Translatable.Name("Enable dimension rules")
    var relicDimensionRulesEnabled: Boolean = true
    @Translatable.Name("Dimension rules")
    @Translatable.Desc("Each dimension uses eight values: mob drop multiplier %, then Common, Uncommon, Rare, Epic, Mythical, Legendary and Immortal acceptance %. Affects mob drops only.")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var relicDimensionRules: Map<String, List<Int>> = mapOf("minecraft:overworld" to listOf(100, 100, 80, 50, 30, 15, 5, 1), "minecraft:the_nether" to listOf(100, 50, 70, 90, 80, 60, 30, 10), "minecraft:the_end" to listOf(100, 10, 30, 60, 90, 100, 80, 50))

    @Translatable.Name("Advanced: XP recovery and shared kill rewards")
    val advancedXpGroup = me.fzzyhmstrs.fzzy_config.config.ConfigGroup("advancedXp", true)
    @Translatable.Name("Recovery interval (ticks)")
    var restorativeForceTicks: Int = 600
    @Translatable.Name("Recovery multiplier (%)")
    var restorativeForceMultiplier: Int = 110
    @Translatable.Name("XP retained by a chunk after collection (%)")
    @Translatable.Desc("95 retains 95% of the previous factor after an accepted orb; lower values reduce repeated XP farming more strongly.")
    var expNegationFactor: Int = 95
    @Translatable.Name("Passive mob XP")
    var itemXpFromPassive: Int = 10
    @Translatable.Name("Hostile mob XP")
    var itemXpFromHostile: Int = 20
    @Translatable.Name("Boss XP")
    @me.fzzyhmstrs.fzzy_config.config.ConfigGroup.Pop
    var itemXpFromBoss: Int = 100

    fun toSnapshot(): PlayerExConfigSnapshot = PlayerExConfigSnapshot(
        relicDimensionRulesEnabled = relicDimensionRulesEnabled,
        relicDimensionRules = relicDimensionRules,
        relicChestsHaveLoot = relicChestsHaveLoot,
        relicChestChance = relicChestChance,
        relicChestLesserOrbChance = relicChestLesserOrbChance,
        relicChestGreaterOrbChance = relicChestGreaterOrbChance,
        relicChestTomeChance = relicChestTomeChance,
        relicOnlyPlayerKills = relicOnlyPlayerKills,
        relicDragonDropsStone = relicDragonDropsStone,
        relicMobLootChance = relicMobLootChance,
        relicMobRelicWeight = relicMobRelicWeight,
        relicMobPotionWeight = relicMobPotionWeight,
        relicMobLesserOrbWeight = relicMobLesserOrbWeight,
        relicMobGreaterOrbWeight = relicMobGreaterOrbWeight,
        relicMobTomeWeight = relicMobTomeWeight,
        relicMobBlacklist = relicMobBlacklist,
        disableUI = disableUI,
        resetOnDeath = resetOnDeath,
        weaponLevelingEnabled = weaponLevelingEnabled,
        armorLevelingEnabled = armorLevelingEnabled,
        weaponMaxLevel = weaponMaxLevel,
        armorMaxLevel = armorMaxLevel,
        weaponDamagePerLevel = weaponDamagePerLevel,
        armorPerLevel = armorPerLevel,
        armorReductionPerLevel = armorReductionPerLevel,
        armorMaxReduction = armorMaxReduction,
        weaponXpFromPassive = weaponXpFromPassive,
        weaponXpFromHostile = weaponXpFromHostile,
        weaponXpFromBoss = weaponXpFromBoss,
        armorXpFromPassive = armorXpFromPassive,
        armorXpFromHostile = armorXpFromHostile,
        armorXpFromBoss = armorXpFromBoss,
        itemXpFromPassive = itemXpFromPassive,
        itemXpFromHostile = itemXpFromHostile,
        itemXpFromBoss = itemXpFromBoss,
        healthRegenerationLifecycle = healthRegenerationLifecycle,
        messageOnItemBreak = messageOnItemBreak,
        destroyCurseOfBinding = destroyCurseOfBinding,
        itemBreakingEnabled = itemBreakingEnabled,
        infiniteItemBreaking = infiniteItemBreaking,
        timesItemCanBreak = timesItemCanBreak,
        skillPointsPerLevelUp = skillPointsPerLevelUp,
        restorativeForceTicks = restorativeForceTicks,
        restorativeForceMultiplier = restorativeForceMultiplier,
        expNegationFactor = expNegationFactor,
        levelFormula = levelFormula.get(),
        weaponFormula = weaponFormula.get(),
        armorFormula = armorFormula.get()
    )

    override fun onSyncClient() {
        PlayerExConfigState.replace(toSnapshot())
    }

    override fun onUpdateClient() {
        PlayerExConfigState.replace(toSnapshot())
    }

    override fun onUpdateServer(context: ServerUpdateContext) {
        PlayerExConfigState.replace(toSnapshot())
    }

    override fun fileType(): FileType = FileType.JSON5
}
