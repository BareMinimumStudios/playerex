package com.bibireden.playerex.api.attribute

import com.bibireden.playerex.PlayerEX
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.RangedAttribute
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object NeoForgePlayerExAttributes {
    private val registrar: DeferredRegister<Attribute> = DeferredRegister.create(
        BuiltInRegistries.ATTRIBUTE,
        PlayerEX.MOD_ID
    )

    init {
        for (definition in (PlayerEXAttributes.DEFINITIONS + PlayerEXAttributes.IRON_DEFINITIONS)) {
            registrar.register(definition.id.path, Supplier<RangedAttribute>(definition::create))
        }
    }

    fun init(modBus: IEventBus) {
        registrar.register(modBus)
        modBus.addListener { event: net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent ->
            // Other mods validate rewards against the default player supplier before datapacks
            // add per-entity instances. Register our baseline there through NeoForge's API.
            val definitions = PlayerEXAttributes.DEFINITIONS +
                if (net.neoforged.fml.ModList.get().isLoaded("irons_spellbooks")) PlayerEXAttributes.IRON_DEFINITIONS else emptyList()
            definitions.forEach { definition ->
                val holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(BuiltInRegistries.ATTRIBUTE.get(definition.id)!!)
                if (!event.has(net.minecraft.world.entity.EntityType.PLAYER, holder)) event.add(net.minecraft.world.entity.EntityType.PLAYER, holder)
            }
        }
    }
}
