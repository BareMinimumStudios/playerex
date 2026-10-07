package com.bibireden.playerex.client

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.item.PlayerExItemComponents
import com.bibireden.playerex.item.RelicEquipment
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.core.registries.BuiltInRegistries

object RelicModels {
    fun register() {
        for (name in RelicEquipment.factories.keys) ItemProperties.register(BuiltInRegistries.ITEM.get(PlayerEX.id(name)), PlayerEX.id("rareness")) { stack, _, _, _ ->
            (stack.get(PlayerExItemComponents.RELIC)?.rarity ?: 0) / 10f
        }
    }
}
