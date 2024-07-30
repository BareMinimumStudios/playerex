package com.bibireden.playerex.keys

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.opc.api.CachedPlayerKey
import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer

class LevelKey : CachedPlayerKey<Int>(PlayerEX.id("level")) {
    override fun get(player: ServerPlayer): Int {
        return DataAttributesAPI.getValue(PlayerEXAttributes.LEVEL, player).map(Double::toInt).orElse(0)
    }

    override fun readFromNbt(tag: CompoundTag): Int {
        return tag.getInt("level")
    }

    override fun writeToNbt(tag: CompoundTag, value: Any) {
        if (value is Int) tag.putInt("level", value)
    }
}