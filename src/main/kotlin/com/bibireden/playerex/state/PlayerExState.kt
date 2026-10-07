package com.bibireden.playerex.state

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

/**
 * Persistent PlayerEx-owned progression state.
 *
 * Levels and allocation counts are the source of truth. Runtime AttributeModifier instances are derived
 * transient state and are reconstructed from this record whenever the player lifecycle requires it.
 */
data class PlayerExState(
    val level: Int = 0,
    val skillPoints: Int = 0,
    val refundablePoints: Int = 0,
    val allocations: Map<ResourceLocation, Int> = emptyMap(),
    val levelUpNotified: Boolean = false
) {
    fun normalized(): PlayerExState = copy(
        level = level.coerceAtLeast(0),
        skillPoints = skillPoints.coerceAtLeast(0),
        refundablePoints = refundablePoints.coerceAtLeast(0),
        allocations = allocations
            .asSequence()
            .filter { (_, value) -> value > 0 }
            .sortedBy { (id, _) -> id.toString() }
            .associateTo(LinkedHashMap()) { (id, value) -> id to value }
    )

    companion object {
        const val MAX_ALLOCATIONS = 256
        val EMPTY = PlayerExState()

        @JvmField
        val CODEC: Codec<PlayerExState> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.optionalFieldOf("level", 0).forGetter(PlayerExState::level),
                Codec.INT.optionalFieldOf("skill_points", 0).forGetter(PlayerExState::skillPoints),
                Codec.INT.optionalFieldOf("refundable_points", 0).forGetter(PlayerExState::refundablePoints),
                Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                    .optionalFieldOf("allocations", emptyMap())
                    .forGetter(PlayerExState::allocations),
                Codec.BOOL.optionalFieldOf("level_up_notified", false).forGetter(PlayerExState::levelUpNotified)
            ).apply(instance, ::PlayerExState)
        }.xmap(PlayerExState::normalized, PlayerExState::normalized)

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerExState> =
            object : StreamCodec<RegistryFriendlyByteBuf, PlayerExState> {
                override fun encode(buffer: RegistryFriendlyByteBuf, value: PlayerExState) {
                    val normalized = value.normalized()
                    buffer.writeVarInt(normalized.level)
                    buffer.writeVarInt(normalized.skillPoints)
                    buffer.writeVarInt(normalized.refundablePoints)
                    require(normalized.allocations.size <= MAX_ALLOCATIONS) {
                        "Too many PlayerEx allocations: ${normalized.allocations.size}"
                    }
                    buffer.writeVarInt(normalized.allocations.size)
                    for ((id, allocation) in normalized.allocations) {
                        buffer.writeResourceLocation(id)
                        buffer.writeVarInt(allocation)
                    }
                    buffer.writeBoolean(normalized.levelUpNotified)
                }

                override fun decode(buffer: RegistryFriendlyByteBuf): PlayerExState {
                    val level = buffer.readVarInt().coerceAtLeast(0)
                    val skillPoints = buffer.readVarInt().coerceAtLeast(0)
                    val refundablePoints = buffer.readVarInt().coerceAtLeast(0)
                    val size = buffer.readVarInt()
                    require(size in 0..MAX_ALLOCATIONS) { "Invalid PlayerEx allocation count: $size" }
                    val allocations = LinkedHashMap<ResourceLocation, Int>(size)
                    repeat(size) {
                        val id = buffer.readResourceLocation()
                        val allocation = buffer.readVarInt()
                        if (allocation > 0) allocations[id] = allocation
                    }
                    return PlayerExState(
                        level = level,
                        skillPoints = skillPoints,
                        refundablePoints = refundablePoints,
                        allocations = allocations,
                        levelUpNotified = buffer.readBoolean()
                    ).normalized()
                }
            }
    }
}
