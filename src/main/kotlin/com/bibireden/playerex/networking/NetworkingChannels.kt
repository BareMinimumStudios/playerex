package com.bibireden.playerex.networking

import com.bibireden.playerex.PlayerEX
import io.wispforest.owo.network.ClientAccess
import io.wispforest.owo.network.OwoNetChannel
import io.wispforest.owo.network.ServerAccess
import kotlin.reflect.KClass

fun <R : Record> OwoNetChannel.registerServerbound(clazz: KClass<R>, handler: OwoNetChannel.ChannelHandler<R, ServerAccess>) {
    this.registerServerbound(clazz.java, handler)
}

fun <R : Record> OwoNetChannel.registerClientbound(clazz: KClass<R>, handler: OwoNetChannel.ChannelHandler<R, ClientAccess>) {
    this.registerClientbound(clazz.java, handler)
}

object NetworkingChannels {
    @JvmField
    val MODIFY = OwoNetChannel.create(PlayerEX.id("modify"))
    @JvmField
    val NOTIFICATIONS = OwoNetChannel.create(PlayerEX.id("notifications"))
}