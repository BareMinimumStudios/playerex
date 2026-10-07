package com.bibireden.playerex.networking.payload

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.networking.type.AttributeMutationType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/** Client intent only. The server must revalidate attribute, amount, caps, points, and refund conditions. */
@JvmRecord
data class AttributeMutationPayload(
    val mutation: AttributeMutationType,
    val attribute: ResourceLocation,
    val amount: Int
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<AttributeMutationPayload> = TYPE

    companion object {
        const val MAX_REQUEST_AMOUNT = 100

        @JvmField
        val TYPE = CustomPacketPayload.Type<AttributeMutationPayload>(PlayerEX.id("attribute_mutation"))

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, AttributeMutationPayload> =
            object : StreamCodec<RegistryFriendlyByteBuf, AttributeMutationPayload> {
                override fun encode(buffer: RegistryFriendlyByteBuf, value: AttributeMutationPayload) {
                    buffer.writeEnum(value.mutation)
                    buffer.writeResourceLocation(value.attribute)
                    buffer.writeVarInt(value.amount)
                }

                override fun decode(buffer: RegistryFriendlyByteBuf): AttributeMutationPayload =
                    AttributeMutationPayload(
                        buffer.readEnum(AttributeMutationType::class.java),
                        buffer.readResourceLocation(),
                        buffer.readVarInt()
                    )
            }
    }
}
