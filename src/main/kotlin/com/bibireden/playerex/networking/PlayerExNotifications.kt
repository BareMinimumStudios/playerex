package com.bibireden.playerex.networking

import com.bibireden.playerex.networking.payload.NotificationPayload
import com.bibireden.playerex.networking.type.NotificationType
import net.minecraft.server.level.ServerPlayer

object PlayerExNotifications {
    @JvmStatic
    fun send(player: ServerPlayer, type: NotificationType) {
        PlatformNetworking.send(player, NotificationPayload(type))
    }
}
