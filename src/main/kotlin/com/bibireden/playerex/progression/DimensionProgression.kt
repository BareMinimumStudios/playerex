package com.bibireden.playerex.progression

import com.bibireden.playerex.config.PlayerExConfigState
import com.bibireden.playerex.math.DimensionCapRules
import com.bibireden.playerex.state.PlayerExState
import com.bibireden.playerex.state.PlayerStateService
import net.minecraft.server.level.ServerPlayer

object DimensionProgression {
    /** Called on the shared server lifecycle, including immediately before a level transaction. */
    fun recordVisit(player: ServerPlayer) {
        val dimension = player.level().dimension().location()
        val state = PlayerStateService.get(player)
        if (dimension in state.visitedDimensions || state.visitedDimensions.size >= PlayerExState.MAX_DIMENSIONS) return
        PlayerStateService.set(player, state.copy(visitedDimensions = state.visitedDimensions + dimension, levelUpNotified = false))
    }

    fun maximum(state: PlayerExState, global: Int): Int {
        val config = PlayerExConfigState.current()
        return DimensionCapRules.maximum(state.visitedDimensions, config.dimensionCapsEnabled,
            config.startingDimensionCap, config.dimensionLevelCaps, global) { it.toString() }
    }
}
