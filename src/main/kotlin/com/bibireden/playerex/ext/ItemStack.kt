package com.bibireden.playerex.ext

import net.minecraft.world.item.ItemStack

var ItemStack.level: Int
    get() = this.orCreateTag.getInt("Level")
    set(value) = this.orCreateTag.putInt("Level", value)

var ItemStack.xp: Int
    get() = this.orCreateTag.getInt("Experience")
    set(value) = this.orCreateTag.putInt("Experience", value)