package com.bibireden.playerex.item

import kotlin.math.ceil
import kotlin.random.Random

/** RelicEx's weighted one-to-five attribute roll, independent of registries and loaders. */
object RelicRollMath {
    data class Weights(
        val relativeWeighting: Double, val additionChance: Double,
        val additionMin: Double, val additionMax: Double, val additionIncrement: Double,
        val multiplierMin: Double, val multiplierMax: Double, val multiplierIncrement: Double
    ) {
        fun valid(): Boolean = listOf(relativeWeighting, additionChance, additionMin, additionMax, additionIncrement,
            multiplierMin, multiplierMax, multiplierIncrement).all(Double::isFinite) &&
            relativeWeighting > 0 && relativeWeighting <= 100 && additionChance in 0.0..100.0 &&
            rangeValid(additionMin, additionMax, additionIncrement) && rangeValid(multiplierMin, multiplierMax, multiplierIncrement)
        private fun rangeValid(min: Double, max: Double, step: Double): Boolean =
            min >= 0 && max > min && max <= 1000000 && step > 0 && ceil((max - min) / step) <= 10000
    }
    data class Roll(val attribute: String, val amount: Double, val multiply: Boolean)
    data class Result(val rarity: Int, val attributes: List<Roll>)
    private data class Value(val amount: Double, val weight: Double)
    class Pool(weights: Map<String, Weights>) {
        private data class Entry(val id: String, val weights: Weights, val additions: List<Value>, val multipliers: List<Value>)
        private val entries = weights.toSortedMap().map { (id, weight) ->
            require(weight.valid()) { "Invalid relic weights for $id" }
            Entry(id, weight, values(weight.additionMin, weight.additionMax, weight.additionIncrement),
                values(weight.multiplierMin, weight.multiplierMax, weight.multiplierIncrement))
        }
        fun roll(random: Random): Result? {
            if (entries.isEmpty()) return null
            val rolled = linkedMapOf<String, Roll>()
            fun attribute(): Double {
                val entry = choose(entries, { it.weights.relativeWeighting }, random)
                val multiply = random.nextDouble() >= entry.weights.additionChance / 100
                val values = if (multiply) entry.multipliers else entry.additions
                val value = choose(values, Value::weight, random)
                val score = if (entry.id in rolled) 1.0 else {
                    rolled[entry.id] = Roll(entry.id, value.amount, multiply)
                    0.5 * value.weight + 0.2 * (if (multiply) 1 - entry.weights.additionChance / 100 else entry.weights.additionChance / 100)
                }
                return score + 0.3 * entry.weights.relativeWeighting / 100
            }
            var weight = attribute()
            val threshold = random.nextDouble()
            repeat(4) { if (threshold <= weight) weight *= attribute() }
            return Result(rarity(weight), rolled.values.toList())
        }
        private fun values(min: Double, max: Double, step: Double): List<Value> =
            (0 until ceil((max - min) / step).toInt()).map { min + it * step }.filter { it < max }
                .map { Value(it, 1 - it / max) }
    }
    fun rarity(weight: Double): Int = when {
        weight >= 0.5 -> 0
        weight >= 0.4 -> 1
        weight >= 0.31 -> 2
        weight >= 0.22 -> 3
        weight >= 0.15 -> 4
        weight >= 0.1 -> 5
        else -> 6
    }
    private fun <T> choose(values: List<T>, weight: (T) -> Double, random: Random): T {
        var target = random.nextDouble() * values.sumOf(weight)
        for (value in values) { target -= weight(value); if (target < 0) return value }
        return values.last()
    }
}
