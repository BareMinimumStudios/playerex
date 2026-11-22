package com.bibireden.playerex.compat.magic

import kotlin.test.*

class IronMagicRulesTest {
    @Test fun requirementsUseOwnedPointsAndExactThresholds() {
        val rules = listOf(0, 10, 30)
        assertTrue(IronMagicRules.canCast(0, 1, rules))
        assertFalse(IronMagicRules.canCast(9, 2, rules))
        assertTrue(IronMagicRules.canCast(10, 2, rules))
        assertFalse(IronMagicRules.canCast(29, 3, rules))
        assertTrue(IronMagicRules.canCast(30, 3, rules))
        assertFalse(IronMagicRules.canCast(Int.MAX_VALUE, 4, rules))
        assertFalse(IronMagicRules.canCast(100, 0, rules))
    }
    @Test fun malformedRequirementsFallBackWithoutCrashing() {
        assertEquals(IronMagicRules.defaultThresholds, IronMagicRules.thresholds(emptyList()))
        assertEquals(IronMagicRules.defaultThresholds, IronMagicRules.thresholds(listOf(10, 0)))
        assertEquals(IronMagicRules.defaultThresholds, IronMagicRules.thresholds(listOf(-1)))
        assertEquals(0, IronMagicRules.allowedLevel(0, listOf(5, 10)))
    }
    @Test fun bonusRulesRejectNonFiniteValuesAndBoundOverflow() {
        assertEquals(0.0, IronMagicRules.bonus(Double.NaN, 1.0))
        assertEquals(0.0, IronMagicRules.bonus(100.0, Double.POSITIVE_INFINITY))
        assertEquals(0.5, IronMagicRules.bonus(100.0, 0.005))
        assertEquals(0.0, IronMagicRules.bonus(-10.0, 1.0))
        assertEquals(1_000_000.0, IronMagicRules.bonus(Double.MAX_VALUE, Double.MAX_VALUE))
    }
}
