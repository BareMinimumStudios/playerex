package com.bibireden.playerex.ui.components.labels

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.IEntityAttribute
import com.bibireden.playerex.ext.id
import com.bibireden.playerex.ui.util.Colors
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.player.Player
import net.minecraft.network.chat.Component

private fun createTextFromAttribute(attribute: Attribute, player: Player) = Component.literal("(")
    .append(Component.literal("${DataAttributesAPI.getValue(attribute, player).map(Double::toInt).orElse(0)}").withStyle {
        it.withColor(Colors.GOLD)
    })
    .append("/${(attribute as IEntityAttribute).`data_attributes$max`().toInt()})")

open class AttributeLabelComponent(private val attribute: Attribute, private val player: Player) : LabelComponent(Component.empty()) {
    init {
        this.horizontalTextAlignment(HorizontalAlignment.CENTER)
        this.verticalTextAlignment(VerticalAlignment.CENTER)

        this.id("${attribute.id}:current_level")

        this.refresh()
    }

    fun refresh(): LabelComponent = text(createTextFromAttribute(attribute, player))
}