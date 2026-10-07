package com.bibireden.playerex.math

import kotlin.math.floor

object GameplayMath {
    @JvmStatic
    fun scaleExperience(amount: Int, multiplier: Double): Int {
        if (amount <= 0) return 0
        if (!multiplier.isFinite()) return amount
        return floor(amount.toDouble() * multiplier.coerceAtLeast(0.0))
            .coerceAtMost(Int.MAX_VALUE.toDouble()).toInt()
    }

    @JvmStatic
    fun scaleDamage(amount: Float, multiplier: Double): Float {
        if (!amount.isFinite() || amount <= 0.0f) return amount
        if (!multiplier.isFinite()) return amount
        return (amount.toDouble() * multiplier.coerceAtLeast(0.0))
            .coerceAtMost(Float.MAX_VALUE.toDouble()).toFloat()
    }
}
