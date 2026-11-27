package com.bibireden.playerex.platform

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.attribute.FabricPlayerExAttributes
import com.bibireden.playerex.api.event.PlayerEXSoundEvents
import com.bibireden.playerex.command.PlayerExCommands
import com.bibireden.playerex.compat.FabricRelicCompat
import com.bibireden.playerex.compat.RemnantCompat
import com.bibireden.playerex.config.Configs
import com.bibireden.playerex.item.PlayerExItemComponents
import com.bibireden.playerex.item.RelicEquipment
import com.bibireden.playerex.item.RelicUtilityItems
import com.bibireden.playerex.item.RelicWeights
import com.bibireden.playerex.networking.FabricNetworking
import com.bibireden.playerex.progression.PlayerProgression
import com.bibireden.playerex.state.FabricAttachments
import com.bibireden.playerex.state.PlayerAttributeReconciler
import com.bibireden.playerex.state.PlayerStateService
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.CreativeModeTabs

object PlayerExFabricEntrypoint : ModInitializer {
    override fun onInitialize() {
        Platform.installModLookup(FabricLoader.getInstance()::isModLoaded)
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PlayerEX.id("item_progression"), PlayerExItemComponents.PROGRESSION)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> PlayerExCommands.register(dispatcher) }
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PlayerEX.id("dragon_stone_users"), PlayerExItemComponents.DRAGON_STONE_USERS)
        val relicFactories = RelicUtilityItems.factories + RelicEquipment.factories
        relicFactories.forEach { (name, factory) ->
            Registry.register(BuiltInRegistries.ITEM, PlayerEX.id(name), factory())
        }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register { entries ->
            relicFactories.keys.forEach { entries.accept(BuiltInRegistries.ITEM.get(PlayerEX.id(it))) }
        }
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PlayerEX.id("relic"), PlayerExItemComponents.RELIC)
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(object : RelicWeights.Reload(), IdentifiableResourceReloadListener {
            override fun getFabricId() = PlayerEX.id("relic_weights")
        })
        FabricRelicCompat.register()
        PlayerEXSoundEvents.events.forEach { (name, sound) -> Registry.register(BuiltInRegistries.SOUND_EVENT, PlayerEX.id(name), sound) }
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
        // Fabric copies persistent attachments in its default respawn phase.
        // Reset and reconcile only after that copy, regardless of mod initialization order.
        val progressionPhase = PlayerEX.id("after_attachment_copy")
        ServerPlayerEvents.AFTER_RESPAWN.addPhaseOrdering(Event.DEFAULT_PHASE, progressionPhase)
        ServerPlayerEvents.AFTER_RESPAWN.register(progressionPhase) { _, newPlayer, alive ->
            PlayerProgression.respawn(newPlayer, alive)
        }
    }
}
