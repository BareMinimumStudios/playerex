package com.bibireden.playerex.api

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation

object PlayerEXCachedKeys {
    @JvmRecord
    data class Level(val level: Int) {
        companion object {
            val CODEC = RecordCodecBuilder.create {
                it.group(Codec.INT.fieldOf("level").forGetter(Level::level)).apply(it, ::Level)
            }
        }
    }

    @JvmField
    val LEVEL_KEY = ResourceLocation.tryBuild("playerex", "level")!!
}