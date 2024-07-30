package com.bibireden.playerex.api.attribute

import com.bibireden.playerex.api.attribute.PlayerEXAttributes.register
import com.bibireden.playerex.ext.id
import net.minecraft.resources.ResourceLocation

object TradeSkillAttributes {
    @JvmField
    val MINING = register("mining", 0.0, 0.0, 100.0);

    @JvmField
    val ENCHANTING = register("enchanting", 0.0, 0.0, 100.0);

    @JvmField
    val ALCHEMY = register("alchemy", 0.0, 0.0, 100.0);

    @JvmField
    val FISHING = register("fishing", 0.0, 0.0, 100.0);

    @JvmField
    val LOGGING = register("logging", 0.0, 0.0, 100.0);

    @JvmField
    val SMITHING = register("smithing", 0.0, 0.0, 100.0);

    @JvmField
    val FARMING = register("farming", 0.0, 0.0, 100.0);

    @JvmField
    val IDS: Set<ResourceLocation> = setOf(
        MINING.id,
        ALCHEMY.id,
        FISHING.id,
        FARMING.id,
        LOGGING.id,
        ENCHANTING.id,
        ENCHANTING.id
    )
}