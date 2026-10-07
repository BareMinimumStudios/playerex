package com.bibireden.playerex.networking

import com.bibireden.playerex.networking.payload.AttributeMutationPayload
import com.bibireden.playerex.networking.payload.LevelRequestPayload
import com.bibireden.playerex.networking.payload.NotificationPayload
import com.bibireden.playerex.networking.payload.PlayerStateSyncPayload
import com.bibireden.playerex.state.ClientPlayerExState
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

object NeoForgeNetworking {
    fun installSenders() {
        PlatformNetworking.installServerSender { player, payload -> PacketDistributor.sendToPlayer(player, payload) }
        PlatformNetworking.installClientSender { payload -> PacketDistributor.sendToServer(payload) }
    }

    fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar("3")

        registrar.playToServer(
            AttributeMutationPayload.TYPE,
            AttributeMutationPayload.STREAM_CODEC
        ) { payload, context ->
            val player = context.player() as? ServerPlayer ?: return@playToServer
            PlayerExRequestRouter.handle(player, payload)
        }

        registrar.playToServer(
            LevelRequestPayload.TYPE,
            LevelRequestPayload.STREAM_CODEC
        ) { payload, context ->
            val player = context.player() as? ServerPlayer ?: return@playToServer
            PlayerExRequestRouter.handle(player, payload)
        }

        registrar.playToClient(
            PlayerStateSyncPayload.TYPE,
            PlayerStateSyncPayload.STREAM_CODEC
        ) { payload, _ ->
            ClientPlayerExState.replace(payload.state)
        }

        registrar.playToClient(
            NotificationPayload.TYPE,
            NotificationPayload.STREAM_CODEC
        ) { payload, _ ->
            ClientNotificationBus.receive(payload.notification)
        }
    }
}
