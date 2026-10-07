package com.bibireden.playerex.platform

import com.bibireden.playerex.networking.FabricClientNetworking
import net.fabricmc.api.ClientModInitializer

object PlayerExFabricClientEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        com.bibireden.playerex.client.PlayerExClientConfig.init()
        com.bibireden.playerex.client.PlayerExClient.init()
        FabricClientNetworking.init()
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES).registerReloadListener(object : net.minecraft.server.packs.resources.ResourceManagerReloadListener, net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener {
            override fun getFabricId() = com.bibireden.playerex.PlayerEX.id("relic_armor_models")
            override fun onResourceManagerReload(manager: net.minecraft.server.packs.resources.ResourceManager) { com.bibireden.playerex.client.RelicArmorModels.clear() }
        })
        com.bibireden.playerex.client.RelicModels.register()
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(com.bibireden.playerex.client.PlayerExClient.openKey)
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register { com.bibireden.playerex.client.PlayerExClient.tick() }
    }
}
