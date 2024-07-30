package com.bibireden.playerex.api.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

object LivingEntityEvents {
    /**
     * Fired before [LivingEntity.heal]; allows the amount healed to be modified before healing happens.
     * Setting the output to `0` is an unreliable way to negate incoming damage depending on other mods installed.
     *
     * @see [LivingEntityEvents.SHOULD_HEAL].
     */
    @JvmField
    val ON_HEAL: Event<Healed> = EventFactory.createArrayBacked(Healed::class.java) { callbacks -> Healed { entity, original ->
        var previous = original
        callbacks.forEach { previous = it.onHeal(entity, previous) }
        previous
    }}

    /**
     * Fired at the start of [LivingEntity.heal], but before healing is applied. Can return false to cancel all healing, or true to allow it.
     */
    @JvmField
    val SHOULD_HEAL: Event<Heal> = EventFactory.createArrayBacked(Heal::class.java) { callbacks -> Heal { entity, original ->
        callbacks.forEach { if (!it.shouldHeal(entity, original)) return@Heal false }
        true
    }}

    /**
     * Fired once at the end of {@link LivingEntity#tick()}, every 20 ticks (1 second).
     */
    @JvmField
    val ON_TICK: Event<Tick> = EventFactory.createArrayBacked(Tick::class.java) { callbacks -> Tick { entity -> callbacks.forEach { it.onTick(entity) } }}

    /**
     * Fired before {@link LivingEntity#damage(DamageSource, float)}; allows the amount of damage to be modified before it is used in any way.
     * Can be used to perform logic prior to the damage method, and can return the original damage to avoid modifying the value.
     * The original value is the incoming damage, followed by the result of this event by any previous registries.
     * Setting the output to 0 is an unreliable way to negate incoming damage depending on other mods installed. Instead, use {@link LivingEntityEvents#SHOUL_DAMAGE}.
     */
    @JvmField
    val ON_DAMAGE: Event<Damaged> = EventFactory.createArrayBacked(Damaged::class.java) { callbacks -> Damaged { entity, source, original ->
        var previous = original
        callbacks.forEach { previous = it.onDamage(entity, source, previous) }
        previous
    }}

    /**
     * Fired after: [LivingEntity.isInvulnerableTo], [net.minecraft.world.World.isClient], [LivingEntity.isDead],
     * ([DamageSource.isFire] && [LivingEntity.hasStatusEffect] for Fire Resistance), and [LivingEntity.isSleeping]
     * is checked, but before all other logic is performed. Can be used to cancel the method and prevent damage from being taken by returning false.
     * Returning true allows the logic to continue.
     */
    @JvmField
    val SHOULD_DAMAGE: Event<Damage> = EventFactory.createArrayBacked(Damage::class.java) { callbacks -> Damage { entity, source, original ->
        callbacks.forEach { if (!it.shouldDamage(entity, source, original)) return@Damage false }
        true
    }}

    fun interface Healed {
        fun onHeal(livingEntity: LivingEntity, original: Float): Float
    }

    fun interface Heal {
        fun shouldHeal(livingEntity: LivingEntity, original: Float): Boolean
    }

    fun interface Tick {
        fun onTick(livingEntity: LivingEntity)
    }

    fun interface Damaged {
        fun onDamage(livingEntity: LivingEntity, source: DamageSource, original: Float): Float
    }

    fun interface Damage {
        fun shouldDamage(livingEntity: LivingEntity, source: DamageSource, original: Float): Boolean
    }
}