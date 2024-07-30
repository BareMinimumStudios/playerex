package com.bibireden.playerex.api.event

import com.bibireden.playerex.PlayerEX
import net.minecraft.sounds.SoundEvent

object PlayerEXSoundEvents {
    val LEVEL_UP_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("level_up"))
    val SPEND_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("spend"))
    val REFUND_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("refund"))
}