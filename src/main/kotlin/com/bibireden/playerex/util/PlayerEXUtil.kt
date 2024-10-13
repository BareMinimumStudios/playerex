package com.bibireden.playerex.util

import com.bibireden.data_attributes.api.util.Maths
import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.PlayerEXTags
import com.bibireden.playerex.ext.level
import com.bibireden.playerex.ext.xp
import com.google.common.collect.Multimap
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round

object PlayerEXUtil {
    private const val VARIABLE = "x"
    /**
     * The function is as follows (according to previous implementation)
     * `stairs(x, stretch, steepness, x-offset, y-offset, y-limit)`
     */
    private val STAIRCASE_FUNCTION = object : Function("stairs", 6) {
        override fun apply(vararg args: Double): Double = min(Maths.stairs(args[0], args[1], args[2], args[3], args[4]), args[5])
    }

    private val expression: Expression
        get() = createExpression()

    @JvmStatic
    private fun createExpression(): Expression {
        return ExpressionBuilder(PlayerEX.CONFIG.levelFormula).variable(VARIABLE).function(STAIRCASE_FUNCTION).build()
    }

    private val weaponExpression: Expression
        get() = createWeaponExpression()

    @JvmStatic
    private fun createWeaponExpression(): Expression {
        return ExpressionBuilder(PlayerEX.CONFIG.weaponLevelingSettings.formula).variable(VARIABLE).build()
    }
    private val armorExpression: Expression
        get() = createArmorExpression()

    @JvmStatic
    private fun createArmorExpression(): Expression {
        return ExpressionBuilder(PlayerEX.CONFIG.armorLevelingSettings.formula).variable(VARIABLE).build()
    }

    @JvmStatic
    /** Computes the experience cost of the provided level. */
    fun getRequiredXpForLevel(player: Player, target: Double): Int {
        val steps = (target - player.level).toInt()
        if (steps <= 0) return 0

        var accumulator = 0
        for (x in 1..steps) {
            val k = steps + player.level
            accumulator += abs(round(expression.setVariable(VARIABLE, k).evaluate())).toInt()
        }

        return accumulator
    }

    /** todo: document, none evident on former, resolve if orElse is needed here, and if we can do nullable or not without drastically changing things */
    @JvmStatic
    fun getRequiredXpForNextLevel(player: Player): Int = getRequiredXpForLevel(player, player.level + 1)

    @JvmStatic
    fun getRequiredXpForLevel(item: ItemStack, target: Int): Int {
        if (target <= item.level) return 0
        var acc = 0
        for (x in item.level+1..target) {
            when {
                isWeapon(item) -> acc += abs(round(weaponExpression.setVariable(VARIABLE, (x).toDouble()).evaluate())).toInt()
                isArmor(item) -> acc += abs(round(armorExpression.setVariable(VARIABLE, (x).toDouble()).evaluate())).toInt()
            }
        }
        return acc
    }

    @JvmStatic
    fun getRequiredXpForNextLevel(item: ItemStack): Int = getRequiredXpForLevel(item, item.level + 1)

    @JvmStatic
    fun getMaxLevel(item: ItemStack): Int {
        return when {
            isWeapon(item) -> PlayerEX.CONFIG.weaponLevelingSettings.maxLevel
            isArmor(item) -> PlayerEX.CONFIG.armorLevelingSettings.maxLevel
            else -> 0
        }
    }

    @JvmStatic
    fun isBroken(stack: ItemStack): Boolean {
        if (stack.tag != null) {
            return stack.tag!!.getBoolean("broken")
        }
        return false
    }

    @JvmStatic
    fun isWeapon(stack: ItemStack): Boolean {
        return stack.`is`(PlayerEXTags.WEAPONS)
    }

    @JvmStatic
    fun isArmor(stack: ItemStack): Boolean {
        return stack.`is`(PlayerEXTags.ARMOR)
    }

    @JvmStatic
    /** Removes an [Attribute], [AttributeModifier] pair from the provided [Multimap] **/
    fun removeModifier(multimap: Multimap<Attribute, AttributeModifier>, attribute: Attribute) {
        val optional = multimap[attribute].stream().findFirst()
        if (optional.isPresent) {
            val modifier = optional.get()
            val newModifier = AttributeModifier(modifier.id, modifier.name, 0.0, modifier.operation)
            multimap.remove(attribute, modifier)
            multimap.put(attribute, newModifier)
        }
    }

    @JvmStatic
    /** Adds a [Double] to the value of an [AttributeModifier] from the provided [Multimap] **/
    fun addToModifier(multimap: Multimap<Attribute, AttributeModifier>, attribute: Attribute, amount: Double) {
        val optional = multimap[attribute].stream().findFirst()
        if (optional.isPresent) {
            val modifier = optional.get()
            val newModifier = AttributeModifier(modifier.id, modifier.name, modifier.amount + amount, modifier.operation)
            multimap.remove(attribute, modifier)
            multimap.put(attribute, newModifier)
        }
    }

    @JvmStatic
    fun isLevelable(stack: ItemStack): Boolean {
        return (isWeapon(stack) && PlayerEX.CONFIG.weaponLevelingSettings.enabled)
                || (isArmor(stack) && PlayerEX.CONFIG.armorLevelingSettings.enabled)
    }

    @JvmStatic
    fun levelItem(itemStack: ItemStack, xpToAdd: Int, maxLevel: Int) {
        if (isLevelable(itemStack)) {
            var nextLevel = getRequiredXpForNextLevel(itemStack)
            itemStack.xp += xpToAdd
            while (itemStack.xp >= nextLevel && itemStack.level < maxLevel) {
                itemStack.xp -= nextLevel
                itemStack.level += 1
                nextLevel = getRequiredXpForNextLevel(itemStack)
            }
        }
    }
}
