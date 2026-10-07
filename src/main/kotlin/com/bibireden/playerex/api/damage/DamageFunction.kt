package com.bibireden.playerex.api.damage
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
fun interface DamageFunction {
    fun apply(livingEntity: LivingEntity, source: DamageSource, damage: Float): Float
}
