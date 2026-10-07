package com.bibireden.playerex.api.event

import com.bibireden.playerex.PlayerEX
import net.minecraft.sounds.SoundEvent

object PlayerEXSoundEvents {
    @JvmField val LEVEL_UP_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("level_up"))
    @JvmField val SPEND_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("spend"))
    @JvmField val REFUND_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("refund"))
    @JvmField val POTION_USE_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("potion_use"))
    val events = mapOf("level_up" to LEVEL_UP_SOUND, "spend" to SPEND_SOUND, "refund" to REFUND_SOUND, "potion_use" to POTION_USE_SOUND)
}
