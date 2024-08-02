package com.bibireden.playerex.api.event

import com.bibireden.playerex.PlayerEX
import net.minecraft.sounds.SoundEvent

object PlayerEXSoundEvents {
    @JvmField
    val LEVEL_UP_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("level_up"))
    @JvmField
    val SPEND_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("spend"))
    @JvmField
    val REFUND_SOUND = SoundEvent.createVariableRangeEvent(PlayerEX.id("refund"))
}