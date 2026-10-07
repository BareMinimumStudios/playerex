package com.bibireden.playerex.networking

import com.bibireden.playerex.networking.payload.AttributeMutationPayload
import com.bibireden.playerex.networking.payload.LevelRequestPayload
import com.bibireden.playerex.networking.payload.NotificationPayload
import com.bibireden.playerex.networking.payload.PlayerStateSyncPayload
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object FabricNetworking {
    fun init() {
        PayloadTypeRegistry.playC2S().register(AttributeMutationPayload.TYPE, AttributeMutationPayload.STREAM_CODEC)
        PayloadTypeRegistry.playC2S().register(LevelRequestPayload.TYPE, LevelRequestPayload.STREAM_CODEC)
        PayloadTypeRegistry.playS2C().register(PlayerStateSyncPayload.TYPE, PlayerStateSyncPayload.STREAM_CODEC)
        PayloadTypeRegistry.playS2C().register(NotificationPayload.TYPE, NotificationPayload.STREAM_CODEC)

        ServerPlayNetworking.registerGlobalReceiver(AttributeMutationPayload.TYPE) { payload, context ->
            PlayerExRequestRouter.handle(context.player(), payload)
        }
        ServerPlayNetworking.registerGlobalReceiver(LevelRequestPayload.TYPE) { payload, context ->
            PlayerExRequestRouter.handle(context.player(), payload)
        }

        PlatformNetworking.installServerSender { player, payload ->
            ServerPlayNetworking.send(player, payload)
        }
    }
}
