package com.bibireden.playerex.item

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RelicRollMathTest {
    private val weights = RelicRollMath.Weights(75.0, 60.0, 1.0, 10.0, 1.0, 0.05, 0.5, 0.01)
    @Test fun rollsAreBoundedDistinctAndDeterministic() {
        val pool = RelicRollMath.Pool((0..9).associate { "test:$it" to weights })
        assertEquals(pool.roll(Random(44)), pool.roll(Random(44)))
        val random = Random(123)
        repeat(2000) {
            val rolled = pool.roll(random)!!
            assertTrue(rolled.rarity in 0..6)
            assertTrue(rolled.attributes.size in 1..5)
            assertEquals(rolled.attributes.size, rolled.attributes.map { it.attribute }.distinct().size)
            rolled.attributes.forEach { value ->
                assertTrue(if (value.multiply) value.amount >= 0.05 && value.amount < 0.5 else value.amount >= 1 && value.amount < 10)
            }
        }
    }
    @Test fun malformedWeightsCannotCreateUnboundedLoops() {
        assertFalse(weights.copy(additionIncrement = 0.0).valid())
        assertFalse(weights.copy(multiplierIncrement = Double.NaN).valid())
        assertFalse(weights.copy(additionIncrement = 0.000001).valid())
        assertFalse(weights.copy(additionMax = weights.additionMin).valid())
        assertFalse(weights.copy(relativeWeighting = -1.0).valid())
        assertNull(RelicRollMath.Pool(emptyMap()).roll(Random(1)))
    }
    @Test fun rarityBoundariesMatchRelicEx() {
        listOf(0.5, 0.4, 0.31, 0.22, 0.15, 0.1, 0.09).forEachIndexed { rarity, weight ->
            assertEquals(rarity, RelicRollMath.rarity(weight))
        }
    }
}
