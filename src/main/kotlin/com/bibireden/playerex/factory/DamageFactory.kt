package com.bibireden.playerex.factory

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.api.damage.DamageFunction
import com.bibireden.playerex.api.damage.DamagePredicate
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.AreaEffectCloud
import net.minecraft.world.entity.projectile.ThrownPotion

object DamageFactory {
    fun forEach(registry: (damagePredicate: DamagePredicate, damageFunction: DamageFunction) -> Unit)
    {
        registry.invoke({ _, source, _ -> source.`is`(DamageTypes.ON_FIRE) }, { entity, _, damage ->
            DataAttributesAPI.getValue(PlayerEXAttributes.FIRE_RESISTANCE, entity).map { damage * (1 - it) }.map(Double::toFloat).orElse(damage)
        })
        registry.invoke({ _, source, _ -> source.`is`(DamageTypes.FREEZE)}, { living, _, damage ->
            DataAttributesAPI.getValue(PlayerEXAttributes.FREEZE_RESISTANCE, living).map { damage * (1 - it) }.map(Double::toFloat).orElse(damage)
        })
        registry.invoke({ _, source, _ -> source.`is`(DamageTypes.LIGHTNING_BOLT)}, { living, _, damage ->
            DataAttributesAPI.getValue(PlayerEXAttributes.LIGHTNING_RESISTANCE, living).map { damage * (1 - it) }.map(Double::toFloat).orElse(damage)
        })
        registry.invoke(
            { living, source, damage -> living.hasEffect(MobEffects.POISON) && source.`is`(DamageTypes.MAGIC) && damage <= 1.0F },
            {living, _, damage -> DataAttributesAPI.getValue(PlayerEXAttributes.POISON_RESISTANCE, living).map { damage * (1 - it) }.map(Double::toFloat).orElse(damage) }
        )
        registry.invoke({_, source, _ -> source.`is`(DamageTypes.WITHER) || (source.`is`(DamageTypes.INDIRECT_MAGIC) && ((source.directEntity is ThrownPotion) || (source.directEntity is AreaEffectCloud))) }, {
            living, source, damage ->
            DataAttributesAPI.getValue(PlayerEXAttributes.WITHER_RESISTANCE, living).map {
                if (source.`is`(DamageTypes.WITHER) && living.isInvertedHealAndHarm) return@map 0.0F
                if (source.`is`(DamageTypes.INDIRECT_MAGIC) && source.directEntity is ThrownPotion && living.isInvertedHealAndHarm) return@map damage
                (damage * 1 - it).toFloat()
            }.orElse(damage)
        })
    }
}