package com.bibireden.playerex.api.attribute
import com.bibireden.playerex.PlayerEX

/** Original passive trade attributes; the original mod supplied no automatic trade XP rules. */
object TradeSkillAttributes {
    @JvmField val MINING_ID = PlayerEX.id("mining")
    @JvmField val ENCHANTING_ID = PlayerEX.id("enchanting")
    @JvmField val ALCHEMY_ID = PlayerEX.id("alchemy")
    @JvmField val FISHING_ID = PlayerEX.id("fishing")
    @JvmField val LOGGING_ID = PlayerEX.id("logging")
    @JvmField val SMITHING_ID = PlayerEX.id("smithing")
    @JvmField val FARMING_ID = PlayerEX.id("farming")
    @JvmField val IDS = setOf(MINING_ID, ENCHANTING_ID, ALCHEMY_ID, FISHING_ID, LOGGING_ID, SMITHING_ID, FARMING_ID)
}
