package com.bibireden.playerex.item

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

data class RelicAttribute(val attribute: ResourceLocation, val amount: Double, val multiply: Boolean) {
    companion object {
        val CODEC: Codec<RelicAttribute> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("attribute").forGetter(RelicAttribute::attribute),
                Codec.DOUBLE.validate { value -> if (value.isFinite() && value in 0.0..1000000.0) DataResult.success(value)
                    else DataResult.error { "Invalid relic amount" } }.fieldOf("amount").forGetter(RelicAttribute::amount),
                Codec.BOOL.optionalFieldOf("multiply", false).forGetter(RelicAttribute::multiply)
            ).apply(instance, ::RelicAttribute)
        }
    }
}
data class RelicState(val rarity: Int, val attributes: List<RelicAttribute>) {
    companion object {
        @JvmField val CODEC: Codec<RelicState> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.intRange(0, 6).fieldOf("rarity").forGetter(RelicState::rarity),
                RelicAttribute.CODEC.listOf().validate { values ->
                    if (values.size in 1..5 && values.map { it.attribute }.distinct().size == values.size) DataResult.success(values)
                    else DataResult.error { "Relics require one to five distinct attributes" }
                }.fieldOf("attributes").forGetter(RelicState::attributes)
            ).apply(instance, ::RelicState)
        }
        @JvmField val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, RelicState> = object : StreamCodec<RegistryFriendlyByteBuf, RelicState> {
            override fun encode(buffer: RegistryFriendlyByteBuf, value: RelicState) {
                require(value.rarity in 0..6 && value.attributes.size in 1..5 && value.attributes.map { it.attribute }.distinct().size == value.attributes.size)
                buffer.writeVarInt(value.rarity)
                buffer.writeVarInt(value.attributes.size)
                value.attributes.forEach {
                    require(it.amount.isFinite() && it.amount in 0.0..1000000.0)
                    buffer.writeResourceLocation(it.attribute)
                    buffer.writeDouble(it.amount)
                    buffer.writeBoolean(it.multiply)
                }
            }
            override fun decode(buffer: RegistryFriendlyByteBuf): RelicState {
                val rarity = buffer.readVarInt()
                val count = buffer.readVarInt()
                require(rarity in 0..6 && count in 1..5) { "Invalid relic state bounds" }
                val attributes = List(count) {
                    val id = buffer.readResourceLocation()
                    val amount = buffer.readDouble()
                    require(amount.isFinite() && amount in 0.0..1000000.0) { "Invalid relic amount" }
                    RelicAttribute(id, amount, buffer.readBoolean())
                }
                require(attributes.map { it.attribute }.distinct().size == count) { "Duplicate relic attributes" }
                return RelicState(rarity, attributes)
            }
        }
    }
}
