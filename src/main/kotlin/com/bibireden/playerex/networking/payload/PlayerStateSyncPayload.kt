package com.bibireden.playerex.networking.payload

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.state.PlayerExState
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

@JvmRecord
data class PlayerStateSyncPayload(val state: PlayerExState) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<PlayerStateSyncPayload> = TYPE

    companion object {
        @JvmField
        val TYPE = CustomPacketPayload.Type<PlayerStateSyncPayload>(PlayerEX.id("state_sync"))

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerStateSyncPayload> =
            object : StreamCodec<RegistryFriendlyByteBuf, PlayerStateSyncPayload> {
                override fun encode(buffer: RegistryFriendlyByteBuf, value: PlayerStateSyncPayload) =
                    PlayerExState.STREAM_CODEC.encode(buffer, value.state)

                override fun decode(buffer: RegistryFriendlyByteBuf): PlayerStateSyncPayload =
                    PlayerStateSyncPayload(PlayerExState.STREAM_CODEC.decode(buffer))
            }
    }
}
