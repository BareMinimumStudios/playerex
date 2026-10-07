package com.bibireden.playerex.progression

import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.config.PlayerExConfigState
import com.bibireden.playerex.math.ProgressionMath
import com.bibireden.playerex.networking.PlayerExNotifications
import com.bibireden.playerex.networking.PlayerExRequestRouter
import com.bibireden.playerex.networking.payload.AttributeMutationPayload
import com.bibireden.playerex.networking.payload.LevelRequestPayload
import com.bibireden.playerex.networking.type.AttributeMutationType
import com.bibireden.playerex.networking.type.NotificationType
import com.bibireden.playerex.state.PlayerAttributeReconciler
import com.bibireden.playerex.state.PlayerExState
import com.bibireden.playerex.state.PlayerStateService
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.RangedAttribute
import kotlin.math.floor
import kotlin.math.round
import com.bibireden.playerex.api.attribute.TradeSkillAttributes

/** Server-authoritative PlayerEx progression transactions. */
object PlayerProgression {
    private var installed = false

    @JvmStatic
    fun install() {
        if (installed) return
        installed = true
        PlayerExRequestRouter.install(::handleAttributeMutation, ::handleLevelRequest)
    }

    private fun handleAttributeMutation(player: ServerPlayer, payload: AttributeMutationPayload) {
        when (payload.mutation) {
            AttributeMutationType.SKILL -> skillUp(player, payload.attribute, payload.amount)
            AttributeMutationType.REFUND -> refund(player, payload.attribute, payload.amount)
        }
    }

    private fun handleLevelRequest(player: ServerPlayer, payload: LevelRequestPayload) {
        levelUp(player, payload.amount)
    }

    @JvmStatic
    fun skillUp(player: ServerPlayer, attributeId: ResourceLocation, amount: Int): Boolean {
        if (amount <= 0 || attributeId !in PlayerEXAttributes.ALLOCATION_ATTRIBUTE_IDS) return false

        if (attributeId in PlayerEXAttributes.SPELL_SCHOOL_IDS && !PlayerEXAttributes.schoolAvailable(player, attributeId)) return false
        val maxAllocation = maxAllocation(attributeId) ?: return false
        val current = PlayerStateService.get(player)
        val existing = current.allocations[attributeId] ?: 0
        val target = existing.toLong() + amount.toLong()

        if (target > maxAllocation.toLong() || current.skillPoints < amount) return false

        val allocations = LinkedHashMap(current.allocations)
        allocations[attributeId] = target.toInt()
        val updated = current.copy(
            skillPoints = current.skillPoints - amount,
            allocations = allocations
        )

        commit(player, updated, NotificationType.SPENT)
        return true
    }

    @JvmStatic
    fun refund(player: ServerPlayer, attributeId: ResourceLocation, amount: Int): Boolean = refundOwned(player, attributeId, amount, false)

    fun refundAdmin(player: ServerPlayer, attributeId: ResourceLocation, amount: Int): Boolean = refundOwned(player, attributeId, amount, true)

    private fun refundOwned(player: ServerPlayer, attributeId: ResourceLocation, amount: Int, admin: Boolean): Boolean {
        if (amount <= 0 || (attributeId !in PlayerEXAttributes.ALLOCATION_ATTRIBUTE_IDS && (!admin || attributeId !in TradeSkillAttributes.IDS))) return false

        val current = PlayerStateService.get(player)
        val existing = current.allocations[attributeId] ?: 0
        if (current.refundablePoints < amount || existing < amount) return false

        val newSkillPoints = current.skillPoints.toLong() + amount.toLong()
        if (newSkillPoints > Int.MAX_VALUE) return false

        val allocations = LinkedHashMap(current.allocations)
        val remaining = existing - amount
        if (remaining == 0) allocations.remove(attributeId) else allocations[attributeId] = remaining

        val updated = current.copy(
            skillPoints = newSkillPoints.toInt(),
            refundablePoints = current.refundablePoints - amount,
            allocations = allocations
        )

        commit(player, updated, NotificationType.REFUNDED)
        return true
    }

    @JvmStatic
    fun levelUp(player: ServerPlayer, amount: Int): Boolean = advanceLevel(player, amount, true)

    /** Relic tomes grant a level without charging vanilla XP. C2S handlers always use levelUp. */
    fun grantLevel(player: ServerPlayer, amount: Int): Boolean = advanceLevel(player, amount, false)

    fun grantRefundPoints(player: ServerPlayer, amount: Int): Boolean {
        if (amount <= 0) return false
        val current = PlayerStateService.get(player)
        val allocated = current.allocations.entries.sumOf { (id, count) ->
            if (id in PlayerEXAttributes.ALLOCATION_ATTRIBUTE_IDS) count.toLong() else 0L
        }.coerceAtMost(Int.MAX_VALUE.toLong())
        val capacity = (allocated + com.bibireden.playerex.registry.RefundConditionRegistry.additionalCapacity(current, player)).coerceAtMost(Int.MAX_VALUE.toLong())
        val total = (current.refundablePoints.toLong() + amount).coerceAtMost(capacity).toInt()
        if (total <= current.refundablePoints) return false
        commit(player, current.copy(refundablePoints = total), NotificationType.REFUNDED)
        return true
    }

    private fun advanceLevel(player: ServerPlayer, amount: Int, chargeXp: Boolean): Boolean {
        if (amount <= 0) return false

        val current = PlayerStateService.get(player)
        val levelAttribute = PlayerEXAttributes.get(PlayerEXAttributes.LEVEL_ID) ?: return false
        val maxLevel = floor(levelAttribute.maxValue).toInt().coerceAtLeast(0)
        val targetLevel = current.level.toLong() + amount.toLong()
        if (targetLevel > maxLevel.toLong()) return false

        val formulas = PlayerExConfigState.formulaEngine()
        val cost = ProgressionMath.costBetweenLevels(current.level, targetLevel.toInt()) { level ->
            formulas.levelCost(level.toDouble())
        }
        if (chargeXp && player.experienceLevel < cost) return false

        val pointsPerLevel = PlayerExConfigState.current().skillPointsPerLevelUp
        val awarded = pointsPerLevel.toLong() * amount.toLong()
        val resultingSkillPoints = current.skillPoints.toLong() + awarded
        if (resultingSkillPoints !in 0..Int.MAX_VALUE.toLong()) return false

        val updated = current.copy(
            level = targetLevel.toInt(),
            skillPoints = resultingSkillPoints.toInt(),
            levelUpNotified = false
        )

        if (chargeXp) player.giveExperienceLevels(-cost)
        commit(player, updated, NotificationType.SPENT)
        return true
    }

    @JvmStatic
    fun checkLevelAvailability(player: ServerPlayer) {
        if (player.tickCount % 20 != 0) return
        val current = PlayerStateService.get(player)
        val max = PlayerEXAttributes.get(PlayerEXAttributes.LEVEL_ID)?.maxValue ?: return
        val available = current.level < max && player.experienceLevel >=
            PlayerExConfigState.formulaEngine().levelCost((current.level + 1).toDouble())
        if (available == current.levelUpNotified) return
        PlayerStateService.set(player, current.copy(levelUpNotified = available))
        if (available) PlayerExNotifications.send(player, NotificationType.LEVEL_UP_AVAILABLE)
    }

    @JvmStatic
    fun respawn(player: ServerPlayer, alive: Boolean) {
        if (!alive && PlayerExConfigState.current().resetOnDeath) {
            PlayerStateService.set(player, PlayerExState.EMPTY, sync = false)
        }
        PlayerAttributeReconciler.reconcile(player)
        PlayerStateService.sync(player)
    }

    // Operator grants share persistence and reconciliation with normal progression,
    // but do not spend the target player's skill points.
    fun grantSkill(player: ServerPlayer, attributeId: ResourceLocation, amount: Int): Boolean {
        if (amount <= 0 || attributeId !in PlayerEXAttributes.ALLOCATION_ATTRIBUTE_IDS + TradeSkillAttributes.IDS) return false
        if (attributeId in PlayerEXAttributes.SPELL_SCHOOL_IDS && !PlayerEXAttributes.schoolAvailable(player, attributeId)) return false
        val maximum = maxAllocation(attributeId) ?: return false
        val current = PlayerStateService.get(player)
        val target = (current.allocations[attributeId] ?: 0).toLong() + amount
        if (target > maximum) return false
        commit(player, current.copy(allocations = current.allocations + (attributeId to target.toInt())), NotificationType.SPENT)
        return true
    }

    fun adjustRefundPoints(player: ServerPlayer, amount: Int) {
        val current = PlayerStateService.get(player)
        val allocated = current.allocations.entries.sumOf { (id, count) ->
            if (id in PlayerEXAttributes.ALLOCATION_ATTRIBUTE_IDS + TradeSkillAttributes.IDS) count.toLong() else 0L
        }
        val capacity = (allocated + com.bibireden.playerex.registry.RefundConditionRegistry.additionalCapacity(current, player)).coerceIn(0, Int.MAX_VALUE.toLong())
        val total = (current.refundablePoints.toLong() + amount).coerceIn(0, capacity).toInt()
        commit(player, current.copy(refundablePoints = total), NotificationType.REFUNDED)
    }

    fun reset(player: ServerPlayer, retain: Int) {
        val current = PlayerStateService.get(player)
        val fraction = retain.coerceIn(0, 100) / 100.0
        // Allocation counts are whole points in the 1.21.1 attachment schema.
        val allocations = current.allocations.mapValues { (_, count) -> (count * fraction).toInt() }.filterValues { it > 0 }
        commit(player, current.copy(level = (current.level * fraction).toInt(),
            skillPoints = round(current.skillPoints * fraction).toInt(),
            refundablePoints = round(current.refundablePoints * fraction).toInt(),
            allocations = allocations, levelUpNotified = false), NotificationType.SPENT)
    }

    private fun maxAllocation(attributeId: ResourceLocation): Int? {
        val attribute = BuiltInRegistries.ATTRIBUTE.get(attributeId) as? RangedAttribute ?: return null
        val maximum = floor(attribute.maxValue)
        return when {
            !maximum.isFinite() -> Int.MAX_VALUE
            maximum <= 0.0 -> 0
            maximum >= Int.MAX_VALUE.toDouble() -> Int.MAX_VALUE
            else -> maximum.toInt()
        }
    }

    private fun commit(player: ServerPlayer, state: PlayerExState, notification: NotificationType) {
        val persisted = PlayerStateService.set(player, state, sync = false)
        PlayerAttributeReconciler.reconcile(player, persisted)
        PlayerStateService.sync(player)
        PlayerExNotifications.send(player, notification)
    }
}
