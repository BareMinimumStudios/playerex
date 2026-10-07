package com.bibireden.playerex.networking

import com.bibireden.playerex.networking.payload.NotificationPayload
import com.bibireden.playerex.networking.payload.PlayerStateSyncPayload
import com.bibireden.playerex.state.ClientPlayerExState
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

object FabricClientNetworking {
    fun init() {
        ClientPlayNetworking.registerGlobalReceiver(PlayerStateSyncPayload.TYPE) { payload, _ ->
            ClientPlayerExState.replace(payload.state)
        }
        ClientPlayNetworking.registerGlobalReceiver(NotificationPayload.TYPE) { payload, _ ->
            ClientNotificationBus.receive(payload.notification)
        }
        PlatformNetworking.installClientSender { payload -> ClientPlayNetworking.send(payload) }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            ClientPlayerExState.clear()
            ClientNotificationBus.clear()
        }
    }
}
