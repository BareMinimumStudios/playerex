package com.bibireden.playerex.compat.iron

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.compat.magic.*
import com.bibireden.playerex.state.PlayerAttributeReconciler
import com.bibireden.playerex.state.PlayerStateService
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent
import io.redspace.ironsspellbooks.api.spells.CastSource
import java.util.WeakHashMap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.tick.PlayerTickEvent

/** Loaded only after the NeoForge entrypoint confirms Iron's is present. No reflection or mixins. */
object NeoForgeIronSpells {
    private val modifier = PlayerEX.id("compat/irons_spells")
    private val previousSettings = WeakHashMap<ServerPlayer, IronMagicSettings>()
    private val nativeAttributes by lazy {
        BuiltInRegistries.ATTRIBUTE.mapNotNull { attribute ->
            val id = BuiltInRegistries.ATTRIBUTE.getKey(attribute)
            if (id?.namespace == "irons_spellbooks") id to BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute) else null
        }.toMap()
    }
    fun register() {
        MagicIntegration.reconcile = ::reconcile
        NeoForge.EVENT_BUS.addListener(::beforeCast)
        NeoForge.EVENT_BUS.addListener { event: PlayerTickEvent.Post ->
            val player = event.entity as? ServerPlayer ?: return@addListener
            if (player.tickCount % 20 == 0) {
                if (previousSettings.put(player, MagicIntegration.settings) !== MagicIntegration.settings)
                    PlayerAttributeReconciler.reconcile(player)
                else reconcile(player)
            }
        }
    }

    fun reconcile(player: ServerPlayer) {
        val bonuses = mutableMapOf<ResourceLocation, Double>()
        val settings = MagicIntegration.settings
        if (MagicIntegration.ironEnabled()) {
            fun add(raw: String, amount: Double) {
                val id = ResourceLocation.tryParse(raw) ?: return
                // The compatibility owns only Iron's attributes, avoiding cycles into primary stats.
                if (id.namespace == "irons_spellbooks" && amount.isFinite()) bonuses.merge(id, amount, Double::plus)
            }
            fun primary(source: ResourceLocation, rules: Map<String, Double>) {
                val value = BuiltInRegistries.ATTRIBUTE.get(source)?.let { player.getAttributeValue(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(it)) } ?: 0.0
                rules.forEach { (target, scale) -> add(target, IronMagicRules.bonus(value, scale)) }
            }
            primary(PlayerEXAttributes.INTELLIGENCE_ID, settings.intelligenceBonuses)
            primary(PlayerEXAttributes.FOCUS_ID, settings.focusBonuses)
            val allocations = PlayerStateService.get(player).allocations
            PlayerEXAttributes.IRON_SCHOOL_IDS.forEach { (allocation, power) ->
                val school = "irons_spellbooks:" + allocation.path.removePrefix("iron_school_")
                add(power.toString(), IronMagicRules.bonus((allocations[allocation] ?: 0).toDouble(), settings.schoolBonuses[school] ?: 0.0))
            }
        }
        // Remove obsolete targets too, including after config edits, disabling or refunding.
        nativeAttributes.forEach { (id, holder) ->
            val instance = player.getAttribute(holder) ?: return@forEach
            val amount = bonuses[id] ?: 0.0
            if (amount == 0.0) instance.removeModifier(modifier)
            else if (instance.getModifier(modifier)?.amount != amount)
                instance.addOrUpdateTransientModifier(AttributeModifier(modifier, amount, AttributeModifier.Operation.ADD_VALUE))
        }
    }

    private fun beforeCast(event: SpellPreCastEvent) {
        val player = event.entity as? ServerPlayer ?: return
        val settings = MagicIntegration.settings
        if (!MagicIntegration.ironEnabled() || !settings.restrictSpellLevels) return
        if (settings.creativeBypass && player.isCreative) return
        if (event.castSource !in setOf(CastSource.SPELLBOOK, CastSource.SWORD, CastSource.SCROLL)) return
        if (event.castSource == CastSource.SCROLL && !settings.restrictScrolls) return
        if (event.spellId in settings.exemptSpells) return
        val school = event.schoolType.id
        val allocation = PlayerEXAttributes.IRON_SCHOOL_IDS.keys.firstOrNull { it.path == "iron_school_${school.path}" && school.namespace == "irons_spellbooks" } ?: return
        val points = PlayerStateService.get(player).allocations[allocation] ?: 0
        val thresholds = settings.requirements(school.toString())
        if (!IronMagicRules.canCast(points, event.spellLevel, thresholds)) {
            event.isCanceled = true
            player.displayClientMessage(Component.translatable("playerex.magic.school_required", event.schoolType.displayName,
                event.spellLevel, IronMagicRules.allowedLevel(points, thresholds)), true)
        }
    }
}
