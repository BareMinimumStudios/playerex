package com.bibireden.playerex.compat.magic

import com.bibireden.playerex.platform.Platform
import net.minecraft.server.level.ServerPlayer

/** Common code never links Iron's classes. Its optional adapter lives only in NeoForge. */
object MagicIntegration {
    @Volatile var settings = IronMagicSettings()
        private set
    var reconcile: (ServerPlayer) -> Unit = {}
    fun update(value: IronMagicSettings) { settings = value }
    fun rpgEnabled(): Boolean = settings.mode in setOf(MagicMode.AUTO, MagicMode.RPG_SERIES, MagicMode.BOTH)
    fun ironEnabled(): Boolean = Platform.isModLoaded("neoforge") && Platform.isModLoaded("irons_spellbooks") &&
        settings.mode in setOf(MagicMode.AUTO, MagicMode.IRONS_SPELLS, MagicMode.BOTH)
}
