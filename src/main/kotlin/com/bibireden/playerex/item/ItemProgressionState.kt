package com.bibireden.playerex.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

data class ItemProgressionState(val level: Int = 0, val experience: Int = 0, val timesBroken: Int = 0, val broken: Boolean = false) {
    fun normalized(): ItemProgressionState = copy(level = level.coerceIn(0, 10000), experience = experience.coerceAtLeast(0), timesBroken = timesBroken.coerceAtLeast(0))

    companion object {
        @JvmField val EMPTY = ItemProgressionState()
        @JvmField val CODEC: Codec<ItemProgressionState> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.intRange(0, 10000).optionalFieldOf("level", 0).forGetter(ItemProgressionState::level),
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("experience", 0).forGetter(ItemProgressionState::experience),
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("times_broken", 0).forGetter(ItemProgressionState::timesBroken),
                Codec.BOOL.optionalFieldOf("broken", false).forGetter(ItemProgressionState::broken)
            ).apply(instance, ::ItemProgressionState)
        }
        @JvmField val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ItemProgressionState> = object : StreamCodec<RegistryFriendlyByteBuf, ItemProgressionState> {
            override fun encode(buffer: RegistryFriendlyByteBuf, value: ItemProgressionState) {
                val state = value.normalized()
                buffer.writeVarInt(state.level)
                buffer.writeVarInt(state.experience)
                buffer.writeVarInt(state.timesBroken)
                buffer.writeBoolean(state.broken)
            }
            override fun decode(buffer: RegistryFriendlyByteBuf): ItemProgressionState {
                val level = buffer.readVarInt()
                val xp = buffer.readVarInt()
                val breaks = buffer.readVarInt()
                require(level in 0..10000 && xp >= 0 && breaks >= 0) { "Invalid PlayerEx item progression" }
                return ItemProgressionState(level, xp, breaks, buffer.readBoolean())
            }
        }
    }
}
