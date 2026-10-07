package com.bibireden.playerex.client

import com.bibireden.playerex.PlayerEX
import net.minecraft.client.Minecraft
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import java.util.Locale

/** Reads the required Data Attributes mod's public, unobfuscated API. No game fields are reflected.
 * Presentation lookups are cached once and kept separate from loader-specific packaging.
 */
internal object AttributePresentation {
    private class Api {
        val attribute = Class.forName("net.bms.data_attributes.api.attribute.IAttribute")
        val children = attribute.getMethod("data_attributes\$children")
        val formula = attribute.getMethod("data_attributes\$formula")
        val max = attribute.getMethod("data_attributes\$max")
        private val function = Class.forName("net.bms.data_attributes.config.functions.AttributeFunction")
        val enabled = function.getMethod("getEnabled")
        val behavior = function.getMethod("getBehavior")
        val value = function.getMethod("getValue")
        val formatted = Class.forName("net.bms.data_attributes.api.DataAttributesAPI").methods.first {
            it.name == "getFormattedValue" && it.parameterCount == 2 &&
                !java.util.function.Supplier::class.java.isAssignableFrom(it.parameterTypes[0])
        }
    }
    private val api = runCatching { Api() }.onFailure {
        PlayerEX.LOGGER.warn("Data Attributes presentation API unavailable; using numeric GUI values", it)
    }.getOrNull()

    fun name(id: ResourceLocation): Component = BuiltInRegistries.ATTRIBUTE.get(id)?.let {
        Component.translatable(it.descriptionId)
    } ?: Component.literal(id.toString())

    private val displayLabels = mapOf(
        "playerex:health_regeneration" to "regen", "playerex:heal_amplification" to "healing_bonus",
        "minecraft:generic.movement_speed" to "movement", "minecraft:generic.attack_speed" to "attack_rate",
        "minecraft:player.entity_interaction_range" to "attack_reach", "minecraft:player.block_interaction_range" to "block_reach"
    )
    fun displayName(id: ResourceLocation): Component = displayLabels[id.toString()]?.let {
        Component.translatable("playerex.screen.stat_$it")
    } ?: name(id)

    fun value(entity: LivingEntity, id: ResourceLocation): Double? = BuiltInRegistries.ATTRIBUTE.get(id)?.let {
        entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(it))?.value
    }

    fun maximum(id: ResourceLocation): Double? = BuiltInRegistries.ATTRIBUTE.get(id)?.let {
        runCatching { api?.max?.invoke(it) as? Double }.getOrNull()
    }

    fun formatted(entity: LivingEntity, id: ResourceLocation, current: Double? = value(entity, id)): String {
        val attribute = BuiltInRegistries.ATTRIBUTE.get(id) ?: return "—"
        if (current == null) return "—"
        return runCatching {
            api?.formatted?.invoke(null, BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), entity) as? String
        }.getOrNull() ?: number(current)
    }

    fun displayed(entity: LivingEntity, id: ResourceLocation, current: Double? = value(entity, id)): String {
        if (current == null) return "—"
        return when (id.toString()) {
            "playerex:health_regeneration" -> number(current * 20 / com.bibireden.playerex.config.PlayerExConfigState.current().healthRegenerationLifecycle.interval) + " hp/s"
            "spell_power:critical_chance" -> number((current - 100.0).coerceIn(0.0, 100.0)) + "%"
            "spell_power:critical_damage", "spell_power:haste" -> number(current) + "%"
            "minecraft:generic.attack_speed" -> number(current) + "/s"
            "minecraft:player.entity_interaction_range", "minecraft:player.block_interaction_range" -> number(current) + " blocks"
            "minecraft:generic.movement_speed" -> number(current / 0.1 * 100) + "%"
            else -> formatted(entity, id, current)
        }
    }

    fun tooltip(entity: LivingEntity, id: ResourceLocation): List<Component> {
        val lines = mutableListOf<Component>(Component.translatable("playerex.screen.attribute_value", displayName(id), displayed(entity, id)))
        lines += Component.translatable("playerex.screen.attribute_effects").withStyle(ChatFormatting.GRAY)
        if (Minecraft.getInstance().options.advancedItemTooltips)
            lines += Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY)
        if (id.toString() == "minecraft:generic.movement_speed")
            lines += Component.translatable("playerex.screen.movement_hint").withStyle(ChatFormatting.GRAY)
        val attribute = BuiltInRegistries.ATTRIBUTE.get(id) ?: return lines
        val access = api ?: return lines
        runCatching {
            val relationships = access.children.invoke(attribute) as? Map<*, *> ?: return@runCatching
            var headingAdded = false
            for ((child, function) in relationships) {
                if (child !is Attribute || function == null || access.enabled.invoke(function) != true) continue
                val childId = BuiltInRegistries.ATTRIBUTE.getKey(child) ?: continue
                if (!headingAdded) {
                    lines += Component.translatable("playerex.screen.modified_attributes").withStyle(ChatFormatting.GREEN)
                    headingAdded = true
                }
                val coefficient = access.value.invoke(function) as Double
                val operation = if (access.behavior.invoke(function).toString() == "Multiply") "multiply" else "add"
                val stacking = access.formula.invoke(child).toString().lowercase(Locale.ROOT)
                lines += Component.translatable("playerex.screen.relationship_$operation", name(childId),
                    number(coefficient), stacking, formatted(entity, childId)).withStyle(ChatFormatting.AQUA)
            }
        }.onFailure { if (lines.size <= 3) lines += Component.translatable("playerex.screen.relationship_unavailable") }
        return lines
    }

    fun number(value: Double): String = String.format(Locale.ROOT, "%.3f", value).trimEnd('0').trimEnd('.')
}
