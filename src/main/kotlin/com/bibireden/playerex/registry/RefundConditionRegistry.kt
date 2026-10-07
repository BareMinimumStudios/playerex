package com.bibireden.playerex.registry
import com.bibireden.playerex.state.PlayerExState
import net.minecraft.world.entity.player.Player

typealias RefundCondition = (PlayerExState, Player) -> Double
object RefundConditionRegistry {
    @Volatile private var entries: List<RefundCondition> = emptyList()
    @JvmStatic @Synchronized fun register(condition: RefundCondition) { entries = entries + condition }
    @JvmStatic fun get(): List<RefundCondition> = entries.toList()
    fun additionalCapacity(state: PlayerExState, player: Player): Long {
        var capacity = 0L
        for (condition in entries) {
            val value = condition(state, player)
            if (value.isFinite() && value > 0) capacity = (capacity + value.coerceAtMost(Int.MAX_VALUE.toDouble()).toLong()).coerceAtMost(Int.MAX_VALUE.toLong())
        }
        return capacity
    }
}
