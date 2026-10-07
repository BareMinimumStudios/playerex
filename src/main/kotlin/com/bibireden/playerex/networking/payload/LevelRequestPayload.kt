package com.bibireden.playerex.networking.payload

import com.bibireden.playerex.PlayerEX
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

/** Client intent only. Server-side progression logic determines whether the request is legal. */
@JvmRecord
data class LevelRequestPayload(val amount: Int) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<LevelRequestPayload> = TYPE

    companion object {
        const val MAX_REQUEST_AMOUNT = 100

        @JvmField
        val TYPE = CustomPacketPayload.Type<LevelRequestPayload>(PlayerEX.id("level_request"))

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, LevelRequestPayload> =
            object : StreamCodec<RegistryFriendlyByteBuf, LevelRequestPayload> {
                override fun encode(buffer: RegistryFriendlyByteBuf, value: LevelRequestPayload) {
                    buffer.writeVarInt(value.amount)
                }
                override fun decode(buffer: RegistryFriendlyByteBuf): LevelRequestPayload = LevelRequestPayload(buffer.readVarInt())
            }
    }
}
