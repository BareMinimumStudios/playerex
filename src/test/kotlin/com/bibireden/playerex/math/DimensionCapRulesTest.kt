package com.bibireden.playerex.math

import kotlin.test.*

class DimensionCapRulesTest {
    private val caps = mapOf("minecraft:overworld" to 10, "minecraft:the_nether" to 20, "minecraft:the_end" to -1)
    @Test fun visitsUnlockHigherCapsWithoutRelockingOnReturn() {
        assertEquals(10, DimensionCapRules.maximum(listOf("minecraft:overworld"), true, 10, caps, 100))
        assertEquals(20, DimensionCapRules.maximum(listOf("minecraft:overworld", "minecraft:the_nether"), true, 10, caps, 100))
        assertEquals(20, DimensionCapRules.maximum(listOf("minecraft:the_nether", "minecraft:overworld"), true, 10, caps, 100))
    }
    @Test fun unlistedDimensionsDoNotUnlockAndUncappedUsesTheGlobalLimit() {
        assertEquals(10, DimensionCapRules.maximum(listOf("mod:unknown"), true, 10, caps, 100))
        assertEquals(100, DimensionCapRules.maximum(listOf("minecraft:the_end"), true, 10, caps, 100))
        assertEquals(15, DimensionCapRules.maximum(listOf("minecraft:the_nether"), true, 10, caps, 15))
    }
    @Test fun disabledRulesAndConfigChangesRemainPredictable() {
        assertEquals(100, DimensionCapRules.maximum(emptyList(), false, 10, caps, 100))
        assertEquals(30, DimensionCapRules.maximum(listOf("mod:realm"), true, 10, mapOf("mod:realm" to 30), 100))
        assertEquals(5, DimensionCapRules.maximum(listOf("minecraft:the_nether"), true, 10, caps, 5))
        assertEquals(0, DimensionCapRules.maximum(emptyList(), true, -1, caps, -5))
    }
}
