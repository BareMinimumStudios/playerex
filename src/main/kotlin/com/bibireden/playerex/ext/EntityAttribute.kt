package com.bibireden.playerex.ext

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.Attribute


val Attribute.id: ResourceLocation
    get() = BuiltInRegistries.ATTRIBUTE.getKey(this)!!