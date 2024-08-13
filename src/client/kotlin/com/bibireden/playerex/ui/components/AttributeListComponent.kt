package com.bibireden.playerex.ui.components

import com.bibireden.data_attributes.api.attribute.EntityAttributeSupplier
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.world.entity.player.Player
import net.minecraft.network.chat.Component
import kotlin.jvm.optionals.getOrNull

class AttributeListComponent(translationKey: String, private val player: Player, val attributes: List<EntityAttributeSupplier>) : FlowLayout(Sizing.fill(25), Sizing.content(), Algorithm.VERTICAL) {
    val entriesSection: FlowLayout

    init {
        child(Components.label(Component.translatable(translationKey)).horizontalSizing(Sizing.fill(100)))
        child(Components.box(Sizing.fill(100), Sizing.fixed(2)))
        entriesSection = Containers.verticalFlow(Sizing.fill(100), Sizing.content())
            .apply { gap(4) }.also(::child)

        gap(4)
        refresh()
    }

    fun refresh() {
        entriesSection.children().filterIsInstance<AttributeListEntryComponent>().forEach(::removeChild)
        entriesSection.children(attributes.mapNotNull { it.get().getOrNull() }.map {
            Containers.horizontalScroll(Sizing.fill(100), Sizing.content(), AttributeListEntryComponent(it, player)).scrollbarThiccness(2)
        })
    }
}