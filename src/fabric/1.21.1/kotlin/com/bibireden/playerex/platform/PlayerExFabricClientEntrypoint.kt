package com.bibireden.playerex.platform

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.client.PlayerExClient
import com.bibireden.playerex.client.PlayerExClientConfig
import com.bibireden.playerex.client.RelicArmorModels
import com.bibireden.playerex.client.RelicModels
import com.bibireden.playerex.networking.FabricClientNetworking
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

object PlayerExFabricClientEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        PlayerExClientConfig.init()
        PlayerExClient.init()
        FabricClientNetworking.init()
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(object : ResourceManagerReloadListener, IdentifiableResourceReloadListener {
            override fun getFabricId() = PlayerEX.id("relic_armor_models")
            override fun onResourceManagerReload(manager: ResourceManager) { RelicArmorModels.clear() }
        })
        RelicModels.register()
        KeyBindingHelper.registerKeyBinding(PlayerExClient.openKey)
        ClientTickEvents.END_CLIENT_TICK.register { PlayerExClient.tick() }
    }
}
