package com.bibireden.playerex.ext

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.components.PlayerEXComponents
import com.bibireden.playerex.components.player.IPlayerDataComponent
import com.bibireden.playerex.util.PlayerEXUtil
import net.minecraft.world.entity.player.Player

val Player.level: Double
    get() = DataAttributesAPI.getValue(PlayerEXAttributes.LEVEL, this).orElse(1.0)

val Player.data: IPlayerDataComponent
    get() = this.getComponent(PlayerEXComponents.PLAYER_DATA)

fun Player.canLevelUp(amount: Int = 1): Boolean {
    return this.experienceLevel >= PlayerEXUtil.getRequiredXpForLevel(this, this.level + amount)
}