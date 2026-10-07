package com.bibireden.playerex.platform

import com.bibireden.playerex.command.PlayerExCommands
import com.bibireden.playerex.item.PlayerExItemComponents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.attribute.FabricPlayerExAttributes
import com.bibireden.playerex.compat.RemnantCompat
import com.bibireden.playerex.config.Configs
import com.bibireden.playerex.networking.FabricNetworking
import com.bibireden.playerex.state.FabricAttachments
import com.bibireden.playerex.state.PlayerAttributeReconciler
import com.bibireden.playerex.state.PlayerStateService
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader

object PlayerExFabricEntrypoint : ModInitializer {
    override fun onInitialize() {
        Platform.installModLookup(FabricLoader.getInstance()::isModLoaded)
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PlayerEX.id("item_progression"), PlayerExItemComponents.PROGRESSION)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> PlayerExCommands.register(dispatcher) }
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PlayerEX.id("dragon_stone_users"), PlayerExItemComponents.DRAGON_STONE_USERS)
        (com.bibireden.playerex.item.RelicUtilityItems.factories + com.bibireden.playerex.item.RelicEquipment.factories).forEach { (name, factory) ->
            Registry.register(BuiltInRegistries.ITEM, PlayerEX.id(name), factory())
        }
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES).register { entries ->
            (com.bibireden.playerex.item.RelicUtilityItems.factories.keys + com.bibireden.playerex.item.RelicEquipment.factories.keys).forEach { entries.accept(BuiltInRegistries.ITEM.get(PlayerEX.id(it))) }
        }
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PlayerEX.id("relic"), PlayerExItemComponents.RELIC)
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.server.packs.PackType.SERVER_DATA).registerReloadListener(object : com.bibireden.playerex.item.RelicWeights.Reload(), net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener {
            override fun getFabricId() = PlayerEX.id("relic_weights")
        })
        com.bibireden.playerex.compat.FabricRelicCompat.register()
        com.bibireden.playerex.api.event.PlayerEXSoundEvents.events.forEach { (name, sound) -> Registry.register(BuiltInRegistries.SOUND_EVENT, PlayerEX.id(name), sound) }
        FabricPlayerExAttributes.init()
        FabricAttachments.init()
        FabricNetworking.init()
        Configs.init()
        PlayerEX.init()
        RemnantCompat.register()

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            PlayerAttributeReconciler.reconcileOnLogin(handler.player)
            PlayerStateService.sync(handler.player)
        }
        ServerPlayerEvents.AFTER_RESPAWN.register { _, newPlayer, alive ->
            com.bibireden.playerex.progression.PlayerProgression.respawn(newPlayer, alive)
        }
    }
}
