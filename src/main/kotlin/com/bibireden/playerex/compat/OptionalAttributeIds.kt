package com.bibireden.playerex.compat

import net.minecraft.resources.ResourceLocation

/**
 * Optional-mod attribute IDs are data, not Java/Kotlin linkages.
 * Keeping these as ResourceLocations means missing optional mods cannot cause
 * classloading failures on either Fabric or NeoForge.
 */
object OptionalAttributeIds {
    @JvmField val SPELL_HASTE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("spell_power", "haste")
    @JvmField val SPELL_CRITICAL_CHANCE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("spell_power", "critical_chance")
    @JvmField val SPELL_CRITICAL_DAMAGE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("spell_power", "critical_damage")
    @JvmField val SPELL_GENERIC_RESISTANCE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("spell_power", "resistance.generic")

    @JvmField val CRITICAL_STRIKE_CHANCE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("critical_strike", "chance")
    @JvmField val CRITICAL_STRIKE_DAMAGE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("critical_strike", "damage")

    @JvmField val RANGED_HASTE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("ranged_weapon", "haste")
    @JvmField val RANGED_DAMAGE: ResourceLocation = ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")
}
