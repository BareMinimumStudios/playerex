package com.bibireden.playerex.compat

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.item.RelicEquipment
import com.google.common.collect.HashMultimap
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

object FabricRelicCompat {
    fun register() {
        if (!FabricLoader.getInstance().isModLoaded("trinkets")) return
        val api = Class.forName("dev.emi.trinkets.api.TrinketsApi")
        val trinket = Class.forName("dev.emi.trinkets.api.Trinket")
        val slotId = Class.forName("dev.emi.trinkets.api.SlotReference").getMethod("getId")
        val register = api.methods.single { it.name == "registerTrinket" }
        val adapter = Proxy.newProxyInstance(trinket.classLoader, arrayOf(trinket)) { proxy, method, rawArgs ->
            val args = rawArgs ?: emptyArray()
            when (method.name) {
                "tick", "onEquip" -> { RelicEquipment.initialize(args[0] as ItemStack, args[2] as LivingEntity); null }
                "getModifiers" -> HashMultimap.create<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier>().apply {
                    RelicEquipment.modifiers(args[0] as ItemStack, "trinkets/${slotId.invoke(args[1])}").forEach { (attribute, modifier) -> put(attribute, modifier) }
                }
                "toString" -> "PlayerEx relic trinket"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args[0]
                else -> InvocationHandler.invokeDefault(proxy, method, *args)
            }
        }
        for (name in listOf("ring_relic", "amulet_relic")) register.invoke(null, BuiltInRegistries.ITEM.get(PlayerEX.id(name)), adapter)
    }
}
