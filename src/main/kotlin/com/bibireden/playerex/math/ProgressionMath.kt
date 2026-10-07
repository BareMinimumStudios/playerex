package com.bibireden.playerex.math

/** Pure progression calculations; deliberately independent of Minecraft classes. */
object ProgressionMath {
    @JvmStatic
    fun costBetweenLevels(currentLevel: Int, targetLevel: Int, costForLevel: (Int) -> Int): Int {
        if (targetLevel <= currentLevel) return 0
        var total = 0L
        for (level in (currentLevel + 1)..targetLevel) {
            total += costForLevel(level).toLong()
            if (total >= Int.MAX_VALUE) return Int.MAX_VALUE
        }
        return total.toInt()
    }
}
