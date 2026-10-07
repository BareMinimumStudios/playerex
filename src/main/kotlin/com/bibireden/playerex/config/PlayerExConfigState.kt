package com.bibireden.playerex.config

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.math.FormulaEngine

/** Loader-neutral, runtime-safe config snapshot. Fzzy types never cross into common code. */
object PlayerExConfigState {
    private val formulas = FormulaEngine()

    @Volatile
    private var snapshot: PlayerExConfigSnapshot = PlayerExConfigSnapshot()

    @JvmStatic
    fun current(): PlayerExConfigSnapshot = snapshot

    @JvmStatic
    fun formulaEngine(): FormulaEngine = formulas

    /**
     * Publishes a config snapshot while retaining the previous known-good formula for any expression
     * that fails Crunch compilation. A bad config edit cannot break progression at runtime.
     */
    @JvmStatic
    fun replace(candidate: PlayerExConfigSnapshot) {
        val normalizedCandidate = candidate.normalized()
        val previous = snapshot

        val level = acceptFormula(
            "level",
            normalizedCandidate.levelFormula,
            previous.levelFormula,
            formulas::updateLevelFormula
        )
        val weapon = acceptFormula(
            "weapon",
            normalizedCandidate.weaponFormula,
            previous.weaponFormula,
            formulas::updateWeaponFormula
        )
        val armor = acceptFormula(
            "armor",
            normalizedCandidate.armorFormula,
            previous.armorFormula,
            formulas::updateArmorFormula
        )

        snapshot = normalizedCandidate.copy(
            levelFormula = level,
            weaponFormula = weapon,
            armorFormula = armor
        )
    }

    private fun acceptFormula(
        name: String,
        candidate: String,
        fallback: String,
        compiler: (String) -> Result<Unit>
    ): String {
        val normalized = FormulaEngine.normalizeLegacyFormula(candidate)
        return compiler(normalized).fold(
            onSuccess = { normalized },
            onFailure = {
                PlayerEX.LOGGER.error("Rejected invalid PlayerEx {} formula '{}'; keeping '{}'", name, candidate, fallback, it)
                fallback
            }
        )
    }
}
