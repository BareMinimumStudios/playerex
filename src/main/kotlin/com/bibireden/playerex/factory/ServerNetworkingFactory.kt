package com.bibireden.playerex.factory

import com.bibireden.playerex.networking.NetworkingChannels
import com.bibireden.playerex.networking.NetworkingPackets
import com.bibireden.playerex.networking.types.NotificationType
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerLoginPacketListenerImpl
import net.minecraft.world.entity.player.Player

object ServerNetworkingFactory {
    @JvmStatic
    fun onLoginQueryStart(handler: ServerLoginPacketListenerImpl, server: MinecraftServer, sender: PacketSender, synchronizer: ServerLoginNetworking.LoginSynchronizer) {
        // todo: requires a config implementation :: requires synchronization at this point, but i think this is automatically handled by owo so probably no need for this, so what's the point of even including this function in the first place i'm going to to crash out
    }

    /** Notifies the given player about a specific [NotificationType]. */
    @JvmStatic
    fun notify(player: Player, type: NotificationType) {
        NetworkingChannels.NOTIFICATIONS.serverHandle(player).send(NetworkingPackets.Notify(type))
    }
}