package com.bibireden.playerex.progression

import com.bibireden.playerex.config.PlayerExConfigSnapshot
import com.bibireden.playerex.config.PlayerExConfigState
import com.bibireden.playerex.state.ChunkExperienceState
import com.bibireden.playerex.state.PlayerStateService
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import kotlin.math.pow

/** Server-side diminishing returns for experience orbs, backed by lazy per-chunk state. */
object ChunkExperienceService {
    /**
     * Applies lazy recovery, then decides whether a newly-created XP orb should be discarded.
     * The caller supplies the random roll so this logic stays deterministic and easy to verify.
     */
    @JvmStatic
    fun shouldDiscardOrb(level: Level, pos: BlockPos, amount: Int, randomRoll: Float): Boolean {
        if (level.isClientSide || amount <= 0) return false

        val chunk = level.getChunk(pos)
        val config = PlayerExConfigState.current()
        val stored = PlayerStateService.chunk(chunk)
        val recovered = recover(stored, level.gameTime, config)

        if (recovered != stored) {
            PlayerStateService.setChunk(chunk, recovered)
        }

        if (randomRoll > recovered.negationFactor) return true

        val multiplier = reductionMultiplier(amount, config.expNegationFactor)
        val reducedFactor = (recovered.negationFactor * multiplier).coerceIn(0.0f, 1.0f)
        if (reducedFactor == recovered.negationFactor) return false

        val recoveryAnchor = when {
            reducedFactor >= 1.0f -> 0L
            recovered.negationFactor >= 1.0f || recovered.lastRecoveryGameTime <= 0L -> level.gameTime
            else -> recovered.lastRecoveryGameTime
        }

        PlayerStateService.setChunk(
            chunk,
            recovered.copy(
                negationFactor = reducedFactor,
                lastRecoveryGameTime = recoveryAnchor
            )
        )
        return false
    }

    internal fun recover(
        state: ChunkExperienceState,
        gameTime: Long,
        config: PlayerExConfigSnapshot
    ): ChunkExperienceState {
        val current = state.normalized()
        if (current.negationFactor >= 1.0f) {
            return if (current.lastRecoveryGameTime == 0L) current else ChunkExperienceState.EMPTY
        }

        val interval = config.restorativeForceTicks.toLong().coerceAtLeast(1L)
        val anchor = current.lastRecoveryGameTime
        if (anchor <= 0L || gameTime <= anchor) {
            return current.copy(lastRecoveryGameTime = gameTime.coerceAtLeast(0L))
        }

        val elapsedIntervals = (gameTime - anchor) / interval
        if (elapsedIntervals <= 0L) return current

        if (current.negationFactor <= 0.0f) {
            return current.copy(lastRecoveryGameTime = anchor + (elapsedIntervals * interval))
        }

        val recoveryMultiplier = config.restorativeForceMultiplier / 100.0
        val scaledFactor = current.negationFactor.toDouble() * recoveryMultiplier.pow(elapsedIntervals.toDouble())
        val recoveredFactor = when {
            !scaledFactor.isFinite() -> 1.0
            else -> scaledFactor.coerceIn(0.0, 1.0)
        }

        if (recoveredFactor >= 1.0) return ChunkExperienceState.EMPTY

        return current.copy(
            negationFactor = recoveredFactor.toFloat(),
            lastRecoveryGameTime = anchor + (elapsedIntervals * interval)
        )
    }

    internal fun reductionMultiplier(amount: Int, expNegationFactor: Int): Float {
        if (amount <= 0) return 1.0f

        val base = expNegationFactor.coerceIn(0, 100) / 100.0f
        val dynamic = base + ((1.0f - base) * (1.0f - (0.1f * amount.toFloat())))
        return dynamic.coerceIn(0.0f, 1.0f)
    }
}
