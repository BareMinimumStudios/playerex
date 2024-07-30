package com.bibireden.playerex.ui.util

import com.bibireden.playerex.ui.components.FormattingPredicate
import net.minecraft.world.entity.ai.attributes.Attribute

object FormattingPredicates {
    val NORMAL: FormattingPredicate = { "%.2f".format(it) }
    val PERCENT: FormattingPredicate = { "${it.toInt()}%" }
    val PERCENTAGE_MULTIPLY: FormattingPredicate = { "${(it * 100.0).toInt()}%" }
    val PERCENTAGE_DIVIDE: FormattingPredicate = { "${(it / 100.0).toInt()}%" }

    fun fromBaseValue(attribute: Attribute): FormattingPredicate {
        return {
            val result = it - attribute.defaultValue
            var text = "$result"
            if (result > 0) text = "+$result"
            text + "%"
        }
    }
}