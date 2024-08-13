package com.bibireden.playerex.factory

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.ext.component
import com.bibireden.playerex.registry.DamageModificationRegistry
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.AbstractArrow

object EventFactory {
    fun reset(oldPlayer: ServerPlayer, newPlayer: ServerPlayer, isAlive: Boolean)
    {
        newPlayer.component.reset(if (PlayerEX.CONFIG.resetOnDeath) 0 else 100)
    }

    fun healed(entity: LivingEntity, amount: Float): Float
    {
        return DataAttributesAPI.getValue(PlayerEXAttributes.HEAL_AMPLIFICATION, entity).map { (amount * (1 + it)).toFloat() }.orElse(amount)
    }

    fun healthRegeneration(entity: LivingEntity)
    {
        if (!entity.level().isClientSide()) {
            val healthRegenerationOption = DataAttributesAPI.getValue(PlayerEXAttributes.HEALTH_REGENERATION, entity)

            if (healthRegenerationOption.isPresent)
            {
                val healthRegeneration = healthRegenerationOption.get()

                if (healthRegeneration > 0.0 && entity.health < entity.maxHealth)
                {
                    entity.heal(healthRegeneration.toFloat())
                }

                return
            }
        }
    }

    fun onDamage(livingEntity: LivingEntity, source: DamageSource, original: Float): Float
    {
        var amount = original
        for (condition in DamageModificationRegistry.get()) {
            val damage = amount
            amount = condition.provide { predicate, function ->
                return@provide if (predicate.test(livingEntity, source, damage)) {
                    function.apply(livingEntity, source, damage)
                }
                else {
                    damage
                }
            }
        }
        return amount
    }

    fun shouldDamage(livingEntity: LivingEntity, source: DamageSource, original: Float): Boolean
    {
        if (original == 0.0F)
        {
            return true
        }

        val origin: Entity? = source.directEntity
        val attacker: Entity? = source.entity

        if (attacker is LivingEntity && (origin is LivingEntity || origin is AbstractArrow))
        {
            DataAttributesAPI.getValue(PlayerEXAttributes.LIFESTEAL, attacker).ifPresent {
                attacker.heal((original * it).toFloat())
            }
        }

        val evasionOption = DataAttributesAPI.getValue(PlayerEXAttributes.EVASION, livingEntity)

        if (evasionOption.isPresent)
        {
            val chance = livingEntity.random.nextFloat()
            return !(chance < evasionOption.get() && origin is AbstractArrow)
        }

        return true
    }

    fun onCritAttack(player: Player, target: Entity, amount: Float): Float
    {
        if (target !is LivingEntity) return amount

        val meleeCritOption = DataAttributesAPI.getValue(PlayerEXAttributes.MELEE_CRITICAL_DAMAGE, player)

        if (meleeCritOption.isPresent)
        {
            return (amount * (1.0 + (meleeCritOption.get() * 10.0)) / 1.5).toFloat()
        }

        return amount
    }

    fun attackIsCrit(player: Player, target: Entity, original: Boolean): Boolean
    {
        if (target !is LivingEntity) return original

        val critChanceOptional = DataAttributesAPI.getValue(PlayerEXAttributes.MELEE_CRITICAL_CHANCE, player)

        if (critChanceOptional.isPresent)
        {
            val chance = player.random.nextFloat()
            return (chance < critChanceOptional.get()) && !player.onClimbable() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger
        }

        return original
    }

}