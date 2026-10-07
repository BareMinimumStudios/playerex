package com.bibireden.playerex.networking

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.networking.payload.AttributeMutationPayload
import com.bibireden.playerex.networking.payload.LevelRequestPayload
import net.minecraft.server.level.ServerPlayer

/**
 * Keeps payload decoding separate from server-authoritative progression logic.
 * Loader packet callbacks stay thin and route validated intent through this seam.
 */
object PlayerExRequestRouter {
    private var attributeMutationHandler: ((ServerPlayer, AttributeMutationPayload) -> Unit)? = null
    private var levelRequestHandler: ((ServerPlayer, LevelRequestPayload) -> Unit)? = null

    fun install(
        attributeHandler: (ServerPlayer, AttributeMutationPayload) -> Unit,
        levelHandler: (ServerPlayer, LevelRequestPayload) -> Unit
    ) {
        attributeMutationHandler = attributeHandler
        levelRequestHandler = levelHandler
    }

    fun handle(player: ServerPlayer, payload: AttributeMutationPayload) {
        if (payload.amount !in 1..AttributeMutationPayload.MAX_REQUEST_AMOUNT) {
            PlayerEX.LOGGER.warn("Rejected invalid PlayerEx attribute mutation amount {} from {}", payload.amount, player.scoreboardName)
            return
        }
        val handler = attributeMutationHandler ?: return
        handler(player, payload)
    }

    fun handle(player: ServerPlayer, payload: LevelRequestPayload) {
        if (payload.amount !in 1..LevelRequestPayload.MAX_REQUEST_AMOUNT) {
            PlayerEX.LOGGER.warn("Rejected invalid PlayerEx level request amount {} from {}", payload.amount, player.scoreboardName)
            return
        }
        val handler = levelRequestHandler ?: return
        handler(player, payload)
    }
}
