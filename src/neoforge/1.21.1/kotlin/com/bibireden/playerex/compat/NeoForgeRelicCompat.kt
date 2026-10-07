package com.bibireden.playerex.compat

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.item.RelicEquipment
import com.google.common.collect.HashMultimap
import net.neoforged.fml.ModList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

object NeoForgeRelicCompat {
    fun register() {
        if (!ModList.get().isLoaded("curios")) return
        val api = Class.forName("top.theillusivec4.curios.api.CuriosApi")
        val curio = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem")
        val context = Class.forName("top.theillusivec4.curios.api.SlotContext")
        val entity = context.getMethod("entity")
        val identifier = context.getMethod("identifier")
        val index = context.getMethod("index")
        val register = api.methods.single { it.name == "registerCurio" }
        val adapter = Proxy.newProxyInstance(curio.classLoader, arrayOf(curio)) { proxy, method, rawArgs ->
            val args = rawArgs ?: emptyArray()
            when (method.name) {
                "curioTick", "onEquip" -> { RelicEquipment.initialize(args[1] as ItemStack, entity.invoke(args[0]) as LivingEntity); null }
                "getAttributeModifiers" -> HashMultimap.create<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier>().apply {
                    RelicEquipment.modifiers(args[2] as ItemStack, "curios/${identifier.invoke(args[0])}/${index.invoke(args[0])}").forEach { (attribute, modifier) -> put(attribute, modifier) }
                }
                "toString" -> "PlayerEx relic curio"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args[0]
                else -> InvocationHandler.invokeDefault(proxy, method, *args)
            }
        }
        for (name in listOf("ring_relic", "amulet_relic")) register.invoke(null, BuiltInRegistries.ITEM.get(PlayerEX.id(name)), adapter)
    }
}
