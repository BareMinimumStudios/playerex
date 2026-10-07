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
        for (definition in PlayerEXAttributes.DEFINITIONS) {
            registrar.register(definition.id.path, Supplier<RangedAttribute>(definition::create))
        }
    }

    fun init(modBus: IEventBus) {
        registrar.register(modBus)
    }
}
