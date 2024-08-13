package com.bibireden.playerex.ui.components

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.EntityAttributeSupplier
import com.bibireden.data_attributes.api.attribute.IEntityAttribute
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.playerex.components.player.IPlayerDataComponent
import com.bibireden.playerex.ext.id
import com.bibireden.playerex.ui.PlayerEXScreen.ButtonType
import com.bibireden.playerex.ui.components.buttons.AttributeButtonComponent
import com.bibireden.playerex.ui.components.labels.AttributeLabelComponent
import com.bibireden.playerex.ui.util.Colors
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.player.Player
import kotlin.jvm.optionals.getOrNull
import net.minecraft.network.chat.Component

private val StackingBehavior.symbol: String
    get() = if (this == StackingBehavior.Add) "+" else "×"

class AttributeComponent(private val attribute: Attribute, private val player: Player, component: IPlayerDataComponent) : FlowLayout(Sizing.fill(100), Sizing.fixed(18), Algorithm.HORIZONTAL) {
    val label: AttributeLabelComponent

    fun refresh() {
        val entries = DataAttributesAPI.clientManager.functions[attribute.id]
        if (!entries.isNullOrEmpty()) {
            label.tooltip(
                Component.translatable("playerex.ui.main.modified_attributes").also { text ->
                    text.append("\n")
                    text.append(Component.literal(attribute.id.toString()).withStyle(ChatFormatting.DARK_GRAY))
                    text.append("\n\n")
                    entries.forEach { function ->
                        val childAttribute = EntityAttributeSupplier(function.id).get().getOrNull() ?: return@forEach
                        val formula = (childAttribute as IEntityAttribute).`data_attributes$formula`()

                        text.apply {
                            append(Component.translatable(childAttribute.descriptionId).withStyle { it.withColor(Colors.SATURATED_BLUE) })
                            append(" (")
                            append(Component.literal(function.behavior.symbol).withStyle { it.withColor(Colors.DARK_GREEN) })
                            append(Component.literal("${function.value}"))
                            append(Component.literal(":").withStyle(Style.EMPTY.withColor(Colors.DARK_GRAY)))
                            append(Component.literal(formula.name.lowercase()).withStyle { it.withColor(if (formula == StackingFormula.Flat) Colors.SANDY else Colors.IMPISH_RED) })
                            append(")")
                            val decLength = function.value.toString().substringAfter('.').length
                            append(Component.literal(" (%.${decLength}f)\n".format(DataAttributesAPI.getValue(childAttribute, player).orElse(0.0))).withStyle(ChatFormatting.GRAY))
                        }
                    }
                }
            )
        }
    }

    init {
        child(
            Components.label(Component.translatable(attribute.descriptionId))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .sizing(Sizing.content(), Sizing.fill(100))
                .positioning(Positioning.relative(0, 50))
                .id("${attribute.id}:label")
        )

        child(AttributeButtonComponent(attribute, player, component, ButtonType.Remove))
        child(
            AttributeLabelComponent(attribute, player).also { label = it }
                .horizontalSizing(Sizing.fill(34))
        )
        child(AttributeButtonComponent(attribute, player, component, ButtonType.Add))

        horizontalAlignment(HorizontalAlignment.RIGHT)
        verticalAlignment(VerticalAlignment.CENTER)

        refresh()
    }
}