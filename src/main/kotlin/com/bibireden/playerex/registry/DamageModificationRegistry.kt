package com.bibireden.playerex.registry
import com.bibireden.playerex.api.damage.DamageFunction
import com.bibireden.playerex.api.damage.DamagePredicate
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

object DamageModificationRegistry {
    @Volatile private var entries: List<Pair<DamagePredicate, DamageFunction>> = emptyList()
    @JvmStatic @Synchronized fun register(predicate: DamagePredicate, function: DamageFunction) {
        entries = entries + (predicate to function)
    }
    fun apply(entity: LivingEntity, source: DamageSource, damage: Float): Float {
        var result = damage
        for ((predicate, function) in entries) if (predicate.test(entity, source, result)) {
            val changed = function.apply(entity, source, result)
            if (changed.isFinite() && changed >= 0f) result = changed
        }
        return result
    }
}
