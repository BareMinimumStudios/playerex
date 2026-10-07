package com.bibireden.playerex.progression

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.compat.Compatibility
import com.bibireden.playerex.math.GameplayMath
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.AreaEffectCloud
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.entity.projectile.ThrownPotion

/** Shared attribute behavior. The caller keeps loader events and authoritative side checks explicit. */
object PlayerGameplay {
    private val breakingSpeed = PlayerEX.id("breaking_speed")
    private val droppedExperience = PlayerEX.id("dropped_experience_multiplier")
    private val regeneration = PlayerEX.id("health_regeneration")
    private val amplification = PlayerEX.id("heal_amplification")
    private val lifesteal = PlayerEX.id("lifesteal")
    private val evasion = PlayerEX.id("evasion")
    private val fire = PlayerEX.id("fire_resistance")
    private val freeze = PlayerEX.id("freeze_resistance")
    private val lightning = PlayerEX.id("lightning_resistance")
    private val poison = PlayerEX.id("poison_resistance")
    private val wither = PlayerEX.id("wither_resistance")
    private val meleeChance = PlayerEX.id("melee_crit_chance")
    private val meleeDamage = PlayerEX.id("melee_crit_damage")
    private val rangedChance = PlayerEX.id("ranged_crit_chance")
    private val rangedDamage = PlayerEX.id("ranged_crit_damage")

    private fun value(entity: LivingEntity, id: net.minecraft.resources.ResourceLocation, fallback: Double = 0.0): Double {
        val attribute = BuiltInRegistries.ATTRIBUTE.get(id) ?: return fallback
        val instance = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute)) ?: return fallback
        return instance.value.takeIf(Double::isFinite) ?: fallback
    }

    @JvmStatic
    fun breakingSpeed(player: Player, original: Float): Float =
        (original + value(player, breakingSpeed, 1.0) - 1.0).coerceAtLeast(0.0).toFloat()

    @JvmStatic
    fun experienceReward(original: Int, killer: Entity?): Int =
        if (killer is LivingEntity && !killer.level().isClientSide)
            GameplayMath.scaleExperience(original, value(killer, droppedExperience, 1.0)) else original

    @JvmStatic
    fun amplifyHealing(entity: LivingEntity, amount: Float): Float =
        if (entity.level().isClientSide) amount else GameplayMath.scaleDamage(amount, 1.0 + value(entity, amplification))

    @JvmStatic
    fun regenerate(entity: LivingEntity) {
        if (entity.level().isClientSide || entity.tickCount % com.bibireden.playerex.config.PlayerExConfigState.current().healthRegenerationLifecycle.interval != 0 || !entity.isAlive || entity.health >= entity.maxHealth) return
        val amount = value(entity, regeneration)
        if (amount > 0.0) entity.heal(amount.coerceAtMost(Float.MAX_VALUE.toDouble()).toFloat())
    }

    @JvmStatic
    fun evade(entity: LivingEntity, source: DamageSource): Boolean =
        !entity.level().isClientSide && source.directEntity is AbstractArrow &&
            entity.random.nextFloat() < value(entity, evasion).coerceIn(0.0, 1.0)

    @JvmStatic
    fun resist(entity: LivingEntity, source: DamageSource, amount: Float): Float {
        if (entity.level().isClientSide) return amount
        if (entity.isInvertedHealAndHarm) {
            if (source.`is`(DamageTypes.WITHER)) return 0f
            if (source.`is`(DamageTypes.INDIRECT_MAGIC) && source.directEntity is ThrownPotion) return amount
        }
        val id = when {
            source.`is`(DamageTypes.ON_FIRE) -> fire
            source.`is`(DamageTypes.FREEZE) -> freeze
            source.`is`(DamageTypes.LIGHTNING_BOLT) -> lightning
            source.`is`(DamageTypes.MAGIC) && amount <= 1.0f && entity.hasEffect(MobEffects.POISON) -> poison
            source.`is`(DamageTypes.WITHER) -> wither
            source.`is`(DamageTypes.INDIRECT_MAGIC) &&
                (source.directEntity is ThrownPotion || source.directEntity is AreaEffectCloud) -> wither
            else -> return amount
        }
        return GameplayMath.scaleDamage(amount, 1.0 - value(entity, id).coerceIn(-1.0, 1.0))
    }

    @JvmStatic
    fun stealLife(victim: LivingEntity, source: DamageSource, healthBefore: Float, accepted: Boolean) {
        if (!accepted || victim.level().isClientSide) return
        val attacker = source.entity as? LivingEntity ?: return
        if (attacker === victim || !attacker.isAlive ||
            (source.directEntity !is LivingEntity && source.directEntity !is AbstractArrow)) return
        // Heal only for health actually lost: blocked, evaded, absorbed and invulnerable hits give no reward.
        val lost = (healthBefore - victim.health).coerceAtLeast(0.0f)
        val amount = GameplayMath.scaleDamage(lost, value(attacker, lifesteal).coerceIn(0.0, 1.0))
        if (amount > 0.0f) attacker.heal(amount)
    }

    @JvmStatic
    fun hasMeleeCritical(player: Player): Boolean = !Compatibility.useExternalCriticalBackend &&
        BuiltInRegistries.ATTRIBUTE.get(meleeChance)?.let {
            player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(it)) != null
        } == true

    @JvmStatic
    fun meleeCritical(player: Player, target: Entity, original: Boolean, charged: Boolean): Boolean {
        if (player.level().isClientSide || target !is LivingEntity || !hasMeleeCritical(player)) return original
        return charged && !player.onClimbable() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger &&
            player.random.nextFloat() < value(player, meleeChance).coerceIn(0.0, 1.0)
    }

    @JvmStatic
    fun meleeMultiplier(player: Player, original: Float): Float =
        if (player.level().isClientSide || !hasMeleeCritical(player)) original
        else (1.5 + value(player, meleeDamage).coerceAtLeast(0.0)).toFloat()

    @JvmStatic
    fun hasRangedCritical(owner: Entity?): Boolean = owner is LivingEntity &&
        !Compatibility.useExternalCriticalBackend && !owner.level().isClientSide &&
        BuiltInRegistries.ATTRIBUTE.get(rangedChance)?.let {
            owner.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(it)) != null
        } == true

    @JvmStatic
    fun rangedCritical(arrow: AbstractArrow, amount: Float): Float {
        val owner = arrow.owner as? LivingEntity ?: return amount
        if (!hasRangedCritical(owner)) return amount
        val critical = owner.random.nextFloat() < value(owner, rangedChance).coerceIn(0.0, 1.0)
        arrow.isCritArrow = critical
        return if (critical) GameplayMath.scaleDamage(amount, 1.0 + 10.0 * value(owner, rangedDamage).coerceAtLeast(0.0)) else amount
    }
}
