package com.bibireden.playerex.ui.components

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.playerex.ext.id
import com.bibireden.playerex.ui.util.Colors
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.player.Player
import net.minecraft.network.chat.Component

typealias FormattingPredicate = (Double) -> String

class AttributeListEntryComponent(val attribute: Attribute, val player: Player) : LabelComponent(Component.empty()) {
    private val BASE_VALUE_FACTOR_IDS = setOf("ranged_weapon:haste")

    init {
        horizontalTextAlignment(HorizontalAlignment.CENTER)
        verticalTextAlignment(VerticalAlignment.CENTER)

        refresh()
    }

    fun refresh() {
        val formattedValue = if (attribute.id.toString() in BASE_VALUE_FACTOR_IDS) {
            // this is literally to handle an edge case.
            val value = DataAttributesAPI.getValue(attribute, player).orElse(0.0)
            attribute.`data_attributes$format`().function(attribute.defaultValue, attribute.defaultValue * 2, value)
        }
        else DataAttributesAPI.getFormattedValue(attribute, player)

        text(
            Component.translatable(attribute.descriptionId)
                .append(": ")
                .append(Component.literal(formattedValue).withStyle { it.withColor(Colors.GOLD) })
        )
    }
}