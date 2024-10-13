package com.bibireden.playerex.api

import com.bibireden.playerex.PlayerEX
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object PlayerEXTags {
    @JvmField
    val UNBREAKABLE_ITEMS: TagKey<Item> = TagKey.create(Registries.ITEM, PlayerEX.id("unbreakable"))

    @JvmField
    val WEAPONS: TagKey<Item> = TagKey.create(Registries.ITEM, PlayerEX.id("weapons"))

    @JvmField
    val ARMOR: TagKey<Item> = TagKey.create(Registries.ITEM, PlayerEX.id("armor"))
}
