package com.bibireden.playerex.math

/** A visited dimension can only raise the unlocked cap; the global attribute limit remains authoritative. */
object DimensionCapRules {
    fun maximum(visited: Collection<String>, enabled: Boolean, starting: Int, rules: Map<String, Int>, global: Int): Int {
        val ceiling = global.coerceAtLeast(0)
        if (!enabled) return ceiling
        var unlocked = starting.coerceAtLeast(0)
        for (dimension in visited) {
            val cap = rules[dimension] ?: continue
            if (cap == -1) return ceiling
            if (cap >= 0) unlocked = maxOf(unlocked, cap)
        }
        return unlocked.coerceAtMost(ceiling)
    }
}
