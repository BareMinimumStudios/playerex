package com.bibireden.playerex.networking

import com.bibireden.playerex.networking.payload.AttributeMutationPayload
import com.bibireden.playerex.networking.payload.LevelRequestPayload
import com.bibireden.playerex.networking.type.AttributeMutationType
import net.minecraft.resources.ResourceLocation

/** Loader-neutral requests used by the character screen. Packets express intent; the server remains authoritative. */
object PlayerExClientRequests {
    @JvmStatic
    fun skill(attribute: ResourceLocation, amount: Int = 1) {
        if (amount !in 1..AttributeMutationPayload.MAX_REQUEST_AMOUNT) return
        PlatformNetworking.sendToServer(AttributeMutationPayload(AttributeMutationType.SKILL, attribute, amount))
    }

    @JvmStatic
    fun refund(attribute: ResourceLocation, amount: Int = 1) {
        if (amount !in 1..AttributeMutationPayload.MAX_REQUEST_AMOUNT) return
        PlatformNetworking.sendToServer(AttributeMutationPayload(AttributeMutationType.REFUND, attribute, amount))
    }

    @JvmStatic
    fun level(amount: Int = 1) {
        if (amount !in 1..LevelRequestPayload.MAX_REQUEST_AMOUNT) return
        PlatformNetworking.sendToServer(LevelRequestPayload(amount))
    }
}
