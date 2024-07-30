package com.bibireden.playerex.registry

import com.bibireden.playerex.components.player.IPlayerDataComponent
import net.minecraft.world.entity.player.Player

typealias RefundCondition = (IPlayerDataComponent, Player) -> Double

object RefundConditionRegistry {
    private val entries: MutableList<RefundCondition> = mutableListOf()

    /** Registers a [RefundCondition]. */
    fun register(condition: RefundCondition) = this.entries.add(condition)

    /** Provides immutable access to the entire registry. */
    fun get() = this.entries.toList()
}