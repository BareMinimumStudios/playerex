package com.bibireden.playerex.api.damage
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
fun interface DamagePredicate {
    fun test(livingEntity: LivingEntity, source: DamageSource, damage: Float): Boolean
}
