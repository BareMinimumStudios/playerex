package com.bibireden.playerex.ui.components

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.playerex.ui.util.Colors
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.player.Player
import net.minecraft.network.chat.Component

typealias FormattingPredicate = (Double) -> String

class AttributeListEntryComponent(
    val attribute: Attribute,
    val player: Player,
    private val formattingPredicate: FormattingPredicate
) : LabelComponent(Component.empty()) {
    init {
        horizontalTextAlignment(HorizontalAlignment.CENTER)
        verticalTextAlignment(VerticalAlignment.CENTER)

        refresh()
    }

    fun refresh() {
        text(
            Component.translatable(attribute.descriptionId)
                .append(": ")
                .append(Component.literal(
                    DataAttributesAPI.getValue(attribute, player).map { formattingPredicate(it) }
                        .orElse("N/A")).withStyle { it.withColor(Colors.GOLD) }
                )
        )
    }
}