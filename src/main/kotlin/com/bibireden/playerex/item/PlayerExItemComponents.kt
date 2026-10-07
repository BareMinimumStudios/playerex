package com.bibireden.playerex.item

import net.minecraft.core.component.DataComponentType

object PlayerExItemComponents {
    @JvmField val RELIC: DataComponentType<RelicState> = DataComponentType.builder<RelicState>()
        .persistent(RelicState.CODEC).networkSynchronized(RelicState.STREAM_CODEC).build()
    @JvmField val DRAGON_STONE_USERS: DataComponentType<List<java.util.UUID>> = DataComponentType.builder<List<java.util.UUID>>()
        .persistent(DragonStoneUsers.CODEC).networkSynchronized(DragonStoneUsers.STREAM_CODEC).build()
    @JvmField val PROGRESSION: DataComponentType<ItemProgressionState> = DataComponentType.builder<ItemProgressionState>()
        .persistent(ItemProgressionState.CODEC).networkSynchronized(ItemProgressionState.STREAM_CODEC).build()
}
