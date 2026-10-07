package com.bibireden.playerex.client

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.networking.ClientNotificationBus
import com.bibireden.playerex.state.ClientPlayerExState
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent

@EventBusSubscriber(modid = PlayerEX.MOD_ID, value = [Dist.CLIENT], bus = EventBusSubscriber.Bus.MOD)
object NeoForgePlayerExClientKeys {
    @JvmStatic @SubscribeEvent
    fun setup(event: net.neoforged.fml.event.lifecycle.FMLClientSetupEvent) { event.enqueueWork { PlayerExClientConfig.init(); PlayerExClient.init(); RelicModels.register() } }
    @JvmStatic @SubscribeEvent
    fun reload(event: net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent) { event.registerReloadListener(net.minecraft.server.packs.resources.ResourceManagerReloadListener { RelicArmorModels.clear() }) }
    @JvmStatic @SubscribeEvent
    fun register(event: RegisterKeyMappingsEvent) { event.register(PlayerExClient.openKey) }
}

@EventBusSubscriber(modid = PlayerEX.MOD_ID, value = [Dist.CLIENT])
object NeoForgePlayerExClientEvents {
    @JvmStatic @SubscribeEvent
    fun tick(event: ClientTickEvent.Post) { PlayerExClient.tick() }

    @JvmStatic @SubscribeEvent
    fun disconnect(event: ClientPlayerNetworkEvent.LoggingOut) {
        ClientPlayerExState.clear()
        ClientNotificationBus.clear()
    }
}
