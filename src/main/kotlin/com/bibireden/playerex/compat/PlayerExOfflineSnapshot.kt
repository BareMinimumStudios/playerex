package com.bibireden.playerex.compat

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation

/** Compact Remnant snapshot. Remnant is an offline cache, never PlayerEx's live state authority. */
@JvmRecord
data class PlayerExOfflineSnapshot(
    val level: Int,
    val skillPoints: Int,
    val refundablePoints: Int,
    val allocations: Map<ResourceLocation, Int>,
    val visitedDimensions: List<ResourceLocation> = emptyList()
) {
    companion object {
        @JvmField
        val CODEC: Codec<PlayerExOfflineSnapshot> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("level").forGetter(PlayerExOfflineSnapshot::level),
                Codec.INT.optionalFieldOf("skill_points", 0).forGetter(PlayerExOfflineSnapshot::skillPoints),
                Codec.INT.optionalFieldOf("refundable_points", 0).forGetter(PlayerExOfflineSnapshot::refundablePoints),
                Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                    .optionalFieldOf("allocations", emptyMap())
                    .forGetter(PlayerExOfflineSnapshot::allocations),
                ResourceLocation.CODEC.listOf().optionalFieldOf("visited_dimensions", emptyList()).forGetter(PlayerExOfflineSnapshot::visitedDimensions)
            ).apply(instance, ::PlayerExOfflineSnapshot)
        }
    }
}
