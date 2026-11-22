package com.bibireden.playerex.compat.magic

/** Pure progression rules shared by the server gate, synced GUI and tests. */
object IronMagicRules {
    val schools = listOf("fire", "ice", "lightning", "holy", "ender", "blood", "evocation", "nature", "eldritch")
    val defaultThresholds = (0..9).map { it * 10 }
    fun thresholds(values: List<Int>?): List<Int> = values?.takeIf {
        it.isNotEmpty() && it.size <= 1000 && it.all { n -> n >= 0 } && it.zipWithNext().all { (a, b) -> a <= b }
    }?.toList() ?: defaultThresholds
    fun allowedLevel(points: Int, requirements: List<Int>): Int =
        thresholds(requirements).indexOfLast { points.coerceAtLeast(0) >= it } + 1
    fun canCast(points: Int, level: Int, requirements: List<Int>): Boolean =
        level > 0 && level <= allowedLevel(points, requirements)
    fun bonus(points: Double, perPoint: Double): Double =
        if (points.isFinite() && perPoint.isFinite()) (points.coerceIn(0.0, 1_000_000.0) * perPoint.coerceIn(-1000.0, 1000.0)).coerceIn(-1_000_000.0, 1_000_000.0) else 0.0
}

enum class MagicMode { AUTO, RPG_SERIES, IRONS_SPELLS, BOTH, NONE }

data class IronMagicSettings(
    val mode: MagicMode = MagicMode.AUTO,
    val restrictSpellLevels: Boolean = false,
    val restrictScrolls: Boolean = false,
    val creativeBypass: Boolean = true,
    val exemptSpells: Set<String> = emptySet(),
    val schoolRequirements: Map<String, List<Int>> = emptyMap(),
    val intelligenceBonuses: Map<String, Double> = emptyMap(),
    val focusBonuses: Map<String, Double> = emptyMap(),
    val schoolBonuses: Map<String, Double> = emptyMap()
) {
    fun requirements(school: String) = IronMagicRules.thresholds(schoolRequirements[school])
}
