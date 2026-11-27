package com.bibireden.playerex.platform

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.attribute.NeoForgePlayerExAttributes
import com.bibireden.playerex.api.event.PlayerEXSoundEvents
import com.bibireden.playerex.command.PlayerExCommands
import com.bibireden.playerex.compat.NeoForgeRelicCompat
import com.bibireden.playerex.compat.RemnantCompat
import com.bibireden.playerex.compat.iron.NeoForgeIronSpells
import com.bibireden.playerex.config.Configs
import com.bibireden.playerex.item.PlayerExItemComponents
import com.bibireden.playerex.item.RelicEquipment
import com.bibireden.playerex.item.RelicUtilityItems
import com.bibireden.playerex.item.RelicWeights
import com.bibireden.playerex.networking.NeoForgeNetworking
import com.bibireden.playerex.progression.PlayerProgression
import com.bibireden.playerex.state.NeoForgeAttachments
import com.bibireden.playerex.state.PlayerAttributeReconciler
import com.bibireden.playerex.state.PlayerStateService
import java.util.function.Supplier
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.CreativeModeTabs
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.AddReloadListenerEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.registries.DeferredRegister

@Mod(PlayerEX.MOD_ID)
class PlayerExNeoForgeEntrypoint(modBus: IEventBus) {
    init {
        Platform.installModLookup { ModList.get().isLoaded(it) }
        val components = DeferredRegister.createDataComponents(PlayerEX.MOD_ID)
        components.register("item_progression", Supplier { PlayerExItemComponents.PROGRESSION })
        components.register("dragon_stone_users", Supplier { PlayerExItemComponents.DRAGON_STONE_USERS })
        components.register("relic", Supplier { PlayerExItemComponents.RELIC })
        components.register(modBus)
        val relicItems = DeferredRegister.createItems(PlayerEX.MOD_ID)
        val relicFactories = RelicUtilityItems.factories + RelicEquipment.factories
        relicFactories.forEach { (name, factory) -> relicItems.register(name, Supplier { factory() }) }
        relicItems.register(modBus)
        modBus.addListener { event: BuildCreativeModeTabContentsEvent ->
            if (event.tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                relicFactories.keys.forEach { event.accept(BuiltInRegistries.ITEM.get(PlayerEX.id(it))) }
            }
        }
        NeoForge.EVENT_BUS.addListener { event: AddReloadListenerEvent -> event.addListener(RelicWeights.Reload()) }
        modBus.addListener { event: FMLCommonSetupEvent ->
            event.enqueueWork {
                // Fzzy's JSON5 parser is shared; load after parallel mod construction.
                Configs.init()
                NeoForgeRelicCompat.register()
                if (ModList.get().isLoaded("irons_spellbooks")) NeoForgeIronSpells.register()
            }
        }
        val sounds = DeferredRegister.create(Registries.SOUND_EVENT, PlayerEX.MOD_ID)
        PlayerEXSoundEvents.events.forEach { (name, sound) -> sounds.register(name, Supplier { sound }) }
        sounds.register(modBus)
        NeoForge.EVENT_BUS.addListener(::registerCommands)
        NeoForgePlayerExAttributes.init(modBus)
        NeoForgeAttachments.init(modBus)
        NeoForgeNetworking.installSenders()
        modBus.addListener(::registerPayloads)
        NeoForge.EVENT_BUS.addListener(::playerLoggedIn)
        NeoForge.EVENT_BUS.addListener(::playerRespawned)

        PlayerEX.init()
        RemnantCompat.register()
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        PlayerExCommands.register(event.dispatcher)
    }

    private fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        NeoForgeNetworking.registerPayloads(event)
    }

    private fun playerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        PlayerAttributeReconciler.reconcileOnLogin(player)
        PlayerStateService.sync(player)
    }

    private fun playerRespawned(event: PlayerEvent.PlayerRespawnEvent) {
        val player = event.entity as? ServerPlayer ?: return
        PlayerProgression.respawn(player, event.isEndConquered)
    }
}