package com.bibireden.playerex.compat

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.platform.Platform

object Compatibility {
    const val CRITICAL_STRIKE = "critical_strike"
    const val SPELL_POWER = "spell_power"
    const val SPELL_ENGINE = "spell_engine"
    const val RANGED_WEAPON_API = "ranged_weapon_api"
    const val REMNANT = "remnant"

    val criticalStrikeLoaded get() = Platform.isModLoaded(CRITICAL_STRIKE)
    val spellPowerLoaded get() = Platform.isModLoaded(SPELL_POWER)
    val spellEngineLoaded get() = Platform.isModLoaded(SPELL_ENGINE)
    val rangedWeaponApiLoaded get() = Platform.isModLoaded(RANGED_WEAPON_API)
    val remnantLoaded get() = Platform.isModLoaded(REMNANT)

    /**
     * Always delegate crit execution when Critical Strike exists. Its own combat hooks cannot safely coexist
     * with PlayerEx's fallback critical execution without risking duplicate rolls/multipliers.
     */
    val useExternalCriticalBackend get() = criticalStrikeLoaded

    /** Spell Power relationships are data-driven and become inert automatically when the target IDs do not exist. */
    val useSpellPowerCompatibility get() = spellPowerLoaded

    fun logDetectedIntegrations() {
        val integrations = buildList {
            if (criticalStrikeLoaded) add("Critical Strike")
            if (spellPowerLoaded) add("Spell Power")
            if (spellEngineLoaded) add("Spell Engine")
            if (rangedWeaponApiLoaded) add("Ranged Weapon API")
            if (remnantLoaded) add("Remnant")
        }
        PlayerEX.LOGGER.info(
            if (integrations.isEmpty()) "No optional PlayerEx integrations detected"
            else "PlayerEx integrations: ${integrations.joinToString()}"
        )
    }
}
