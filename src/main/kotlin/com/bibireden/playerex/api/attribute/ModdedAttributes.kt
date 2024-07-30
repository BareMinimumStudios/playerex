package com.bibireden.playerex.api.attribute

import com.bibireden.data_attributes.api.attribute.EntityAttributeSupplier
import net.minecraft.resources.ResourceLocation

/**
 * Attributes that specifically do not relate to **PlayerEX**, but are used by the mod.
 *
 * They have a possibility of not existing depending on the mods currently active.
 */
object ModdedAttributes {
    @JvmField
    val SPELL_HASTE = EntityAttributeSupplier(ResourceLocation.tryBuild("spell_power", "haste")!!)

    @JvmField
    val SPELL_CRITICAL_CHANCE = EntityAttributeSupplier(ResourceLocation.tryBuild("spell_power", "critical_chance")!!)
}