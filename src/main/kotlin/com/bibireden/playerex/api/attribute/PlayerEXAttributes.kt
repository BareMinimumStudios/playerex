package com.bibireden.playerex.api.attribute

import com.bibireden.playerex.PlayerEX
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.RangedAttribute

/**
 * Loader-neutral PlayerEx attribute definitions and registry lookups.
 * Registration itself belongs to the Fabric and NeoForge source sets.
 */
object PlayerEXAttributes {
    data class Definition(
        val id: ResourceLocation,
        val base: Double,
        val min: Double,
        val max: Double
    ) {
        fun create(): RangedAttribute = RangedAttribute(
            "attribute.name.${PlayerEX.MOD_ID}.${id.path}",
            base,
            min,
            max
        ).setSyncable(true) as RangedAttribute
    }

    @JvmField val LEVEL_ID = id("level")
    @JvmField val CONSTITUTION_ID = id("constitution")
    @JvmField val STRENGTH_ID = id("strength")
    @JvmField val DEXTERITY_ID = id("dexterity")
    @JvmField val INTELLIGENCE_ID = id("intelligence")
    @JvmField val LUCKINESS_ID = id("luckiness")
    @JvmField val FOCUS_ID = id("focus")

    @JvmField
    val PRIMARY_ATTRIBUTE_IDS: Set<ResourceLocation> = setOf(
        CONSTITUTION_ID,
        STRENGTH_ID,
        DEXTERITY_ID,
        INTELLIGENCE_ID,
        LUCKINESS_ID,
        FOCUS_ID
    )

    // Spell Power and More RPG Library register these magic-school attributes.
    @JvmField
    val SPELL_SCHOOL_IDS: Map<ResourceLocation, ResourceLocation> =
        listOf("fire", "frost", "arcane", "healing", "lightning", "soul", "water", "earth", "air", "nature")
            .associate { id("spell_power_$it") to ResourceLocation.fromNamespaceAndPath("spell_power", it) }

    @JvmField
    val ALLOCATION_ATTRIBUTE_IDS = PRIMARY_ATTRIBUTE_IDS + SPELL_SCHOOL_IDS.keys

    fun schoolAvailable(player: net.minecraft.world.entity.player.Player, allocation: ResourceLocation): Boolean {
        val powerId = SPELL_SCHOOL_IDS[allocation] ?: return false
        val power = BuiltInRegistries.ATTRIBUTE.get(powerId) ?: return false
        return player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(power)) != null
    }

    @JvmField
    val DEFINITIONS: List<Definition> = listOf(
        Definition(LEVEL_ID, 0.0, 0.0, 100.0),
        Definition(CONSTITUTION_ID, 0.0, 0.0, 100.0),
        Definition(STRENGTH_ID, 0.0, 0.0, 100.0),
        Definition(DEXTERITY_ID, 0.0, 0.0, 100.0),
        Definition(INTELLIGENCE_ID, 0.0, 0.0, 100.0),
        Definition(LUCKINESS_ID, 0.0, 0.0, 100.0),
        Definition(FOCUS_ID, 0.0, 0.0, 100.0),
        Definition(id("spell_critical_chance_factor"), 0.0, 0.0, 1.0),
        Definition(id("spell_critical_damage_factor"), 0.0, 0.0, 1.0),
        Definition(id("health_regeneration"), 0.0, 0.0, 1_000_000.0),
        Definition(id("heal_amplification"), 0.0, 0.0, 1.0),
        Definition(id("lifesteal"), 0.0, 0.0, 1.0),
        Definition(id("breaking_speed"), 0.0, 0.0, 100.0),
        Definition(id("fire_resistance"), 0.0, -1.0, 1.0),
        Definition(id("freeze_resistance"), 0.0, -1.0, 1.0),
        Definition(id("lightning_resistance"), 0.0, -1.0, 1.0),
        Definition(id("wither_resistance"), 0.0, -1.0, 1.0),
        Definition(id("poison_resistance"), 0.0, -1.0, 1.0),
        Definition(id("evasion"), 0.0, 0.0, 1.0),
        Definition(id("melee_crit_chance"), 0.0, 0.0, 1.0),
        Definition(id("melee_crit_damage"), 0.0, 0.0, 1.0),
        Definition(id("ranged_crit_chance"), 0.0, 0.0, 1.0),
        Definition(id("ranged_crit_damage"), 0.0, 0.0, 1.0),
        Definition(id("dropped_experience_multiplier"), 1.0, 0.0, 1024.0)
    ) + SPELL_SCHOOL_IDS.keys.map { Definition(it, 0.0, 0.0, Int.MAX_VALUE.toDouble()) } + TradeSkillAttributes.IDS.map { Definition(it, 0.0, 0.0, 100.0) }

    /** Resolves a registered PlayerEx ranged attribute after loader registry setup. */
    @JvmStatic
    fun get(id: ResourceLocation): RangedAttribute? = BuiltInRegistries.ATTRIBUTE.get(id) as? RangedAttribute

    private fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(PlayerEX.MOD_ID, path)
}
