package com.bibireden.playerex.api.damage

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

fun interface DamagePredicate {
    /**
     * Determines, using the input conditions, whether to apply the DamageFunction to the incoming damage to the LivingEntity.
     * @param livingEntity
     * @param source
     * @param damage
     * @return
     */
    fun test(livingEntity: LivingEntity, source: DamageSource, damage: Float): Boolean
}