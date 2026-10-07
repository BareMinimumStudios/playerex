package com.bibireden.playerex.networking.payload

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.networking.type.NotificationType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

@JvmRecord
data class NotificationPayload(val notification: NotificationType) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<NotificationPayload> = TYPE

    companion object {
        @JvmField
        val TYPE = CustomPacketPayload.Type<NotificationPayload>(PlayerEX.id("notification"))

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, NotificationPayload> =
            object : StreamCodec<RegistryFriendlyByteBuf, NotificationPayload> {
                override fun encode(buffer: RegistryFriendlyByteBuf, value: NotificationPayload) {
                    buffer.writeEnum(value.notification)
                }
                override fun decode(buffer: RegistryFriendlyByteBuf): NotificationPayload =
                    NotificationPayload(buffer.readEnum(NotificationType::class.java))
            }
    }
}
