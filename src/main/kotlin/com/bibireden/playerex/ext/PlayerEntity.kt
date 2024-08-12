package com.bibireden.playerex.ext

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.components.PlayerEXComponents
import com.bibireden.playerex.components.player.IPlayerDataComponent
import net.minecraft.world.entity.player.Player

val Player.level: Double
    get() = DataAttributesAPI.getValue(PlayerEXAttributes.LEVEL, this).orElse(1.0)

val Player.component: IPlayerDataComponent
    get() = this.getComponent(PlayerEXComponents.PLAYER_DATA)