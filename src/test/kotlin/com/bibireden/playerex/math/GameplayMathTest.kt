package com.bibireden.playerex.math

import kotlin.test.Test
import kotlin.test.assertEquals

class GameplayMathTest {
    @Test fun experiencePreservesZeroAndBoundsOverflow() {
        assertEquals(0, GameplayMath.scaleExperience(0, 2.0))
        assertEquals(10, GameplayMath.scaleExperience(5, 2.0))
        assertEquals(7, GameplayMath.scaleExperience(5, 1.5))
        assertEquals(0, GameplayMath.scaleExperience(5, -1.0))
        assertEquals(5, GameplayMath.scaleExperience(5, Double.NaN))
        assertEquals(Int.MAX_VALUE, GameplayMath.scaleExperience(Int.MAX_VALUE, 1024.0))
    }

    @Test fun resistancesSupportImmunityAndVulnerability() {
        assertEquals(0.0f, GameplayMath.scaleDamage(10.0f, 0.0))
        assertEquals(5.0f, GameplayMath.scaleDamage(10.0f, 0.5))
        assertEquals(20.0f, GameplayMath.scaleDamage(10.0f, 2.0))
        assertEquals(10.0f, GameplayMath.scaleDamage(10.0f, Double.NaN))
        assertEquals(Float.MAX_VALUE, GameplayMath.scaleDamage(Float.MAX_VALUE, 2.0))
    }

    @Test fun progressionChargesEveryIntermediateLevelAndSaturates() {
        assertEquals(12, ProgressionMath.costBetweenLevels(2, 5) { it })
        assertEquals(0, ProgressionMath.costBetweenLevels(5, 5) { error("Should not evaluate") })
        assertEquals(Int.MAX_VALUE, ProgressionMath.costBetweenLevels(0, 3) { Int.MAX_VALUE })
    }
}
