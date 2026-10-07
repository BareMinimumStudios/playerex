package com.bibireden.playerex.math

import kotlin.math.sin

object PlayerExMath {
    /** Exact legacy Data Attributes staircase function used by PlayerEx 1.20.1. */
    @JvmStatic
    fun stairs(x: Double, stretch: Double, steepness: Double, xOffset: Double, yOffset: Double): Double {
        return steepness * stretch * (x - xOffset) - steepness * sin(stretch * (x - xOffset)) + yOffset
    }
}
