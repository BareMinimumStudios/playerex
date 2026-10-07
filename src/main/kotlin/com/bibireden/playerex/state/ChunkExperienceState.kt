package com.bibireden.playerex.state

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * Lazy chunk XP state; no per-chunk ticking is required.
 * Recovery is calculated from [lastRecoveryGameTime] only when XP logic touches the chunk.
 */
data class ChunkExperienceState(
    val negationFactor: Float = 1.0f,
    val lastRecoveryGameTime: Long = 0L
) {
    fun normalized(): ChunkExperienceState = copy(
        negationFactor = if (negationFactor.isFinite()) negationFactor.coerceIn(0.0f, 1.0f) else 1.0f,
        lastRecoveryGameTime = lastRecoveryGameTime.coerceAtLeast(0L)
    )

    companion object {
        val EMPTY = ChunkExperienceState()

        @JvmField
        val CODEC: Codec<ChunkExperienceState> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.FLOAT.optionalFieldOf("negation_factor", 1.0f).forGetter(ChunkExperienceState::negationFactor),
                Codec.LONG.optionalFieldOf("last_recovery_game_time", 0L).forGetter(ChunkExperienceState::lastRecoveryGameTime)
            ).apply(instance, ::ChunkExperienceState)
        }.xmap(ChunkExperienceState::normalized, ChunkExperienceState::normalized)
    }
}
