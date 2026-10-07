package com.bibireden.playerex.item

import com.mojang.serialization.DataResult
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import java.util.UUID

object DragonStoneUsers {
    const val MAX_USERS = 4096
    val CODEC = UUIDUtil.CODEC.listOf().validate { users ->
        if (users.size > MAX_USERS) DataResult.error { "Too many dragon stone users" }
        else DataResult.success(users.distinct())
    }
    val STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, List<UUID>> {
        override fun encode(buffer: RegistryFriendlyByteBuf, users: List<UUID>) {
            require(users.size <= MAX_USERS)
            buffer.writeVarInt(users.size)
            users.forEach(buffer::writeUUID)
        }
        override fun decode(buffer: RegistryFriendlyByteBuf): List<UUID> {
            val count = buffer.readVarInt()
            require(count in 0..MAX_USERS) { "Invalid dragon stone user count" }
            return List(count) { buffer.readUUID() }.distinct()
        }
    }
}
