package com.bibireden.playerex.api.attribute

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries

object FabricPlayerExAttributes {
    fun init() {
        for (definition in PlayerEXAttributes.DEFINITIONS) {
            Registry.register(BuiltInRegistries.ATTRIBUTE, definition.id, definition.create())
        }
    }
}
