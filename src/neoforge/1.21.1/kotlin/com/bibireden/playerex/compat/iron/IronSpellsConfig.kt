package com.bibireden.playerex.compat.iron

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.compat.magic.*
import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.api.FileType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.event.api.ServerUpdateContext
import me.fzzyhmstrs.fzzy_config.util.Translatable

@RootConfig
@Version(1)
@Translatable.Name("PlayerEx: Magic compatibility")
@Translatable.Desc("NeoForge magic integrations. Changes are synced from the server; Iron's Spells remains optional.")
class IronSpellsConfig : Config(PlayerEX.id("magic")) {
    @Translatable.Name("Active magic integrations")
    @Translatable.Desc("AUTO enables installed systems. RPG_SERIES, IRONS_SPELLS, BOTH or NONE select PlayerEx school allocation and pages; this does not disable another mod's spells.")
    var mode = MagicMode.AUTO

    @Translatable.Name("Require school points for spell levels")
    @Translatable.Desc("Off by default to preserve existing characters. When enabled, the server denies casts above the school's unlocked level.")
    var restrictSpellLevels = false
    @Translatable.Name("Apply requirements to scrolls")
    var restrictScrolls = false
    @Translatable.Name("Creative players bypass requirements")
    var creativeBypass = true
    @Translatable.Name("Spells exempt from requirements")
    @Translatable.Desc("Full spell IDs. Commands and non-player casts are never restricted.")
    var exemptSpells: List<String> = emptyList()
    @Translatable.Name("School level requirements")
    @Translatable.Desc("Full Iron's school ID -> ascending allocated-point thresholds. List entry 1 unlocks spell level 1, entry 2 unlocks level 2, etc. Default: 0,10,...,90. Unknown schools are left unrestricted. Invalid lists use the default thresholds.")
    var schoolRequirements: Map<String, List<Int>> = IronMagicRules.schools.associate { "irons_spellbooks:$it" to IronMagicRules.defaultThresholds }

    @Translatable.Name("Intelligence attribute bonuses")
    @Translatable.Desc("Target attribute ID -> flat bonus per effective Intelligence point. Iron's percentage attributes use 1.0 = 100%, so 0.005 adds half a percentage point. Maximum mana uses whole mana units. Zero disables a bonus.")
    var intelligenceBonuses: Map<String, Double> = mapOf(
        "irons_spellbooks:max_mana" to 2.0,
        "irons_spellbooks:spell_power" to 0.005,
        "irons_spellbooks:cast_time_reduction" to 0.0025,
        "irons_spellbooks:summon_damage" to 0.005
    )
    @Translatable.Name("Focus attribute bonuses")
    @Translatable.Desc("Target attribute ID -> flat bonus per effective Focus point. Iron's percentage attributes use 1.0 = 100%. Zero disables a bonus.")
    var focusBonuses: Map<String, Double> = mapOf(
        "irons_spellbooks:mana_regen" to 0.01,
        "irons_spellbooks:cooldown_reduction" to 0.0025
    )
    @Translatable.Name("School power per allocated point")
    @Translatable.Desc("Full Iron's school ID -> bonus to its native school-power attribute per owned point. Default 0.01 adds one percentage point. Removing an entry disables its power bonus without deleting the player's allocation.")
    var schoolBonuses: Map<String, Double> = IronMagicRules.schools.associate { "irons_spellbooks:$it" to 0.01 }

    fun publish() = MagicIntegration.update(IronMagicSettings(mode, restrictSpellLevels, restrictScrolls, creativeBypass,
        exemptSpells.toSet(), schoolRequirements.mapValues { IronMagicRules.thresholds(it.value) },
        intelligenceBonuses.toMap(), focusBonuses.toMap(), schoolBonuses.toMap()))
    override fun onSyncClient() { publish() }
    override fun onUpdateClient() { publish() }
    override fun onUpdateServer(context: ServerUpdateContext) {
        publish()
        val server = context.getServer()
        server.execute { server.playerList.players.forEach { com.bibireden.playerex.state.PlayerAttributeReconciler.reconcile(it) } }
    }
    override fun fileType(): FileType = FileType.JSON5
}
