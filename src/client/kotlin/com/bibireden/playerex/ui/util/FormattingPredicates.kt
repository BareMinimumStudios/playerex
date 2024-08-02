package com.bibireden.playerex.ui.util

import com.bibireden.playerex.ui.components.FormattingPredicate
import net.minecraft.world.entity.ai.attributes.Attribute
import kotlin.math.round

object FormattingPredicates {
    @JvmField
    val NORMAL: FormattingPredicate = { "%.2f".format(it) }
    @JvmField
    val PERCENTAGE_MULTIPLY: FormattingPredicate = { "${(it * 100.0).toInt()}%" }
    @JvmField
    val PERCENTAGE_DIVIDE: FormattingPredicate = { "${(it / 100.0).toInt()}%" }

    /**
     * Bases calculation and positive/negative evaluation using the [Attribute]'s base as a metric.
     *
     * For example, if an attribute's base value was 100,
     * and the current value of a player's attribute was 150, it is a `+50` increase.
     * */
    @JvmStatic
    fun fromBaseValue(attribute: Attribute, percentage: Boolean): FormattingPredicate {
        return {
            val result = round(it - attribute.defaultValue).toInt()
            var text = "$result"
            if (result > 0) text = "+$result"
            if (percentage) text += "%"
            text
        }
    }
}