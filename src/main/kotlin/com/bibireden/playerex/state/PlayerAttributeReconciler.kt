package com.bibireden.playerex.state

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.api.attribute.TradeSkillAttributes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.AttributeModifier

/** Rebuilds PlayerEx-owned runtime modifiers from persisted progression state. */
object PlayerAttributeReconciler {
    private val levelModifierId = PlayerEX.id("allocation/level")
    private val allocationModifierIds = (PlayerEXAttributes.ALLOCATION_ATTRIBUTE_IDS + TradeSkillAttributes.IDS).associateWith { PlayerEX.id("allocation/${it.path}") }

    @JvmStatic
    fun reconcile(player: ServerPlayer, state: PlayerExState = PlayerStateService.get(player)) {
        reconcileAttribute(player, PlayerEXAttributes.LEVEL_ID, levelModifierId, state.level)

        for (attributeId in allocationModifierIds.keys) {
            reconcileAttribute(
                player,
                attributeId,
                allocationModifierIds.getValue(attributeId),
                state.allocations[attributeId] ?: 0
            )
        }
    }

    @JvmStatic
    fun reconcileOnLogin(player: ServerPlayer) {
        reconcile(player)
        (player as LoadedPlayerHealth).`playerex$restoreLoadedHealth`()
    }

    private fun reconcileAttribute(
        player: ServerPlayer,
        attributeId: ResourceLocation,
        modifierId: ResourceLocation,
        value: Int
    ) {
        val attribute = BuiltInRegistries.ATTRIBUTE.get(attributeId) ?: return
        val holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute)
        val instance = player.getAttribute(holder)
        if (instance == null) {
            PlayerEX.LOGGER.warn(
                "Player {} has no AttributeInstance for {}; PlayerEx could not reconcile it",
                player.scoreboardName,
                BuiltInRegistries.ATTRIBUTE.getKey(attribute)
            )
            return
        }

        val sanitized = attribute.sanitizeValue(value.coerceAtLeast(0).toDouble())
        if (sanitized == 0.0) {
            instance.removeModifier(modifierId)
            return
        }

        instance.addOrUpdateTransientModifier(
            AttributeModifier(modifierId, sanitized, AttributeModifier.Operation.ADD_VALUE)
        )
    }
}
