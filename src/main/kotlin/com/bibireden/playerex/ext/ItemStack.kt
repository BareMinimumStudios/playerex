package com.bibireden.playerex.ext

import net.minecraft.world.item.ItemStack

var ItemStack.level: Int
    get() = this.tag?.getInt("Level") ?: 0
    set(value) {
        if (value == 0 && this.tag == null) return
        this.orCreateTag.putInt("Level", value)
    }

var ItemStack.xp: Int
    get() = this.tag?.getInt("Experience") ?: 0
    set(value) {
        if (value == 0 && this.tag == null) return
        this.orCreateTag.putInt("Experience", value)
    }

var ItemStack.timesBroken: Int
    get() = this.tag?.getInt("TimesBroken") ?: 0
    set(value) {
        if (value == 0 && this.tag == null) return
        this.orCreateTag.putInt("TimesBroken", value)
    }