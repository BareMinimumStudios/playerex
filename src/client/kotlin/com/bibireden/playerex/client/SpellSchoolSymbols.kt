package com.bibireden.playerex.client

import com.bibireden.playerex.PlayerEX
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

/** Packaged pixel glyphs avoid missing emoji in Minecraft's default font. */
internal object SpellSchoolSymbols {
    private val font = PlayerEX.id("schools")
    private val glyphs = listOf("fire", "frost", "arcane", "healing", "lightning", "soul", "water", "earth", "air", "nature")
        .mapIndexed { index, name -> name to Component.literal((0xE100 + index).toChar().toString()).withStyle { it.withFont(font).withColor(0xFFFFFF) } }.toMap()
    fun icon(attribute: ResourceLocation): Component? = when (attribute.namespace) {
        "spell_power" -> glyphs[attribute.path]
        "playerex" -> glyphs[attribute.path.removePrefix("spell_power_")]
        else -> null
    }
}
