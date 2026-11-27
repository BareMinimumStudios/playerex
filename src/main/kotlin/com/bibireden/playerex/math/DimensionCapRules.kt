package com.bibireden.playerex.math

/** A visited dimension can only raise the unlocked cap; the global attribute limit remains authoritative. */
object DimensionCapRules {
    fun maximum(visited: Collection<String>, enabled: Boolean, starting: Int, rules: Map<String, Int>, global: Int): Int =
        maximum(visited, enabled, starting, rules, global) { it }

    internal inline fun <T> maximum(
        visited: Iterable<T>,
        enabled: Boolean,
        starting: Int,
        rules: Map<String, Int>,
        global: Int,
        dimensionKey: (T) -> String
    ): Int {
        val ceiling = global.coerceAtLeast(0)
        if (!enabled) return ceiling
        var unlocked = starting.coerceAtLeast(0)
        if (unlocked >= ceiling) return ceiling
        for (dimension in visited) {
            val cap = rules[dimensionKey(dimension)] ?: continue
            if (cap == -1) return ceiling
            if (cap >= ceiling) return ceiling
            unlocked = maxOf(unlocked, cap)
        }
        return unlocked
    }
}
