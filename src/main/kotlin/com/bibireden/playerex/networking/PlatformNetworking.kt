package com.bibireden.playerex.networking

import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer

/** Loader-neutral packet send bridge. Registration remains loader-specific. */
object PlatformNetworking {
    private var sendToPlayer: ((ServerPlayer, CustomPacketPayload) -> Unit)? = null
    private var sendToServer: ((CustomPacketPayload) -> Unit)? = null

    fun installServerSender(sender: (ServerPlayer, CustomPacketPayload) -> Unit) {
        sendToPlayer = sender
    }

    fun installClientSender(sender: (CustomPacketPayload) -> Unit) {
        sendToServer = sender
    }

    fun send(player: ServerPlayer, payload: CustomPacketPayload) {
        (sendToPlayer ?: error("PlayerEx server networking is not initialized"))(player, payload)
    }

    fun sendToServer(payload: CustomPacketPayload) {
        (sendToServer ?: error("PlayerEx client networking is not initialized"))(payload)
    }
}
