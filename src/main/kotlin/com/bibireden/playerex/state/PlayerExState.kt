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
    val levelUpNotified: Boolean = false,
    val visitedDimensions: List<ResourceLocation> = emptyList()
) {
    fun normalized(): PlayerExState = copy(
        visitedDimensions = visitedDimensions.distinct().sortedBy { it.toString() }.take(MAX_DIMENSIONS),
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
        const val MAX_DIMENSIONS = 1024
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
                Codec.BOOL.optionalFieldOf("level_up_notified", false).forGetter(PlayerExState::levelUpNotified),
                ResourceLocation.CODEC.listOf().optionalFieldOf("visited_dimensions", emptyList()).forGetter(PlayerExState::visitedDimensions)
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
                    buffer.writeVarInt(normalized.visitedDimensions.size)
                    normalized.visitedDimensions.forEach(buffer::writeResourceLocation)
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
                    val notified = buffer.readBoolean()
                    val dimensionCount = buffer.readVarInt()
                    require(dimensionCount in 0..MAX_DIMENSIONS) { "Invalid visited dimension count: $dimensionCount" }
                    val dimensions = List(dimensionCount) { buffer.readResourceLocation() }
                    return PlayerExState(
                        level = level,
                        skillPoints = skillPoints,
                        refundablePoints = refundablePoints,
                        allocations = allocations,
                        levelUpNotified = notified,
                        visitedDimensions = dimensions
                    ).normalized()
                }
            }
    }
}
