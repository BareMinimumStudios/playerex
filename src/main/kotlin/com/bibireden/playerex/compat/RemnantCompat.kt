package com.bibireden.playerex.compat

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.state.PlayerStateService
import net.bms.remnant.api.PlayerLedger
import net.minecraft.world.entity.player.Player

/** Registers PlayerEx's offline view with the required, jar-in-jar Remnant mod. */
object RemnantCompat {
    private var registered = false

    @JvmStatic
    fun register() {
        if (registered) return
        PlayerLedger.register(PlayerEX.id("player"), PlayerExOfflineSnapshot.CODEC, ::snapshot)
        registered = true
        PlayerEX.LOGGER.info("Registered PlayerEx offline snapshot with bundled Remnant")
    }

    private fun snapshot(player: Player): PlayerExOfflineSnapshot {
        val state = PlayerStateService.get(player)
        return PlayerExOfflineSnapshot(state.level, state.skillPoints, state.refundablePoints, state.allocations)
    }
}
