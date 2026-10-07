package com.bibireden.playerex.math

import redempt.crunch.CompiledExpression
import redempt.crunch.Crunch
import redempt.crunch.functional.ExpressionEnv
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Compiles configured formulas once, then reuses the compiled representation.
 * This replaces exp4j and fixes the 1.20.1 behavior that rebuilt an expression
 * every time its property getter was accessed.
 */
class FormulaEngine(
    initialLevelFormula: String = DEFAULT_LEVEL_FORMULA,
    initialWeaponFormula: String = DEFAULT_ITEM_FORMULA,
    initialArmorFormula: String = DEFAULT_ITEM_FORMULA
) {
    private val level = AtomicReference(compileLevel(initialLevelFormula))
    private val weapon = AtomicReference(compileSimple(initialWeaponFormula))
    private val armor = AtomicReference(compileSimple(initialArmorFormula))

    fun updateLevelFormula(formula: String): Result<Unit> = runCatching {
        level.set(compileLevel(normalizeLegacyFormula(formula)))
    }

    fun updateWeaponFormula(formula: String): Result<Unit> = runCatching {
        weapon.set(compileSimple(normalizeLegacyFormula(formula)))
    }

    fun updateArmorFormula(formula: String): Result<Unit> = runCatching {
        armor.set(compileSimple(normalizeLegacyFormula(formula)))
    }

    fun levelCost(levelNumber: Double): Int = evaluate(level.get(), levelNumber)
    fun weaponCost(levelNumber: Double): Int = evaluate(weapon.get(), levelNumber)
    fun armorCost(levelNumber: Double): Int = evaluate(armor.get(), levelNumber)

    private fun evaluate(expression: CompiledExpression, x: Double): Int {
        val value = abs(expression.evaluate(x))
        return when {
            !value.isFinite() -> Int.MAX_VALUE
            value >= Int.MAX_VALUE.toDouble() -> Int.MAX_VALUE
            else -> value.roundToInt().coerceAtLeast(0)
        }
    }

    private fun compileLevel(raw: String): CompiledExpression {
        val env = ExpressionEnv()
            .setVariableNames("x")
            .addFunction("stairs", 6) { args ->
                min(PlayerExMath.stairs(args[0], args[1], args[2], args[3], args[4]), args[5])
            }
        return Crunch.compileExpression(normalizeLegacyFormula(raw), env)
    }

    private fun compileSimple(raw: String): CompiledExpression {
        val env = ExpressionEnv().setVariableNames("x")
        return Crunch.compileExpression(normalizeLegacyFormula(raw), env)
    }

    companion object {
        const val DEFAULT_LEVEL_FORMULA = "stairs(x,0.2,2.4,17,10,25)"
        const val DEFAULT_ITEM_FORMULA = "5*x^1.1"

        /** Known 1.20.1 defaults used exp4j implicit multiplication (`5x`). */
        @JvmStatic
        fun normalizeLegacyFormula(value: String): String = value
            .replace(Regex("(?<=\\d)(?=[A-Za-z(])"), "*")
            .replace(Regex("(?<=\\))(?=[A-Za-z0-9(])"), "*")
    }
}
