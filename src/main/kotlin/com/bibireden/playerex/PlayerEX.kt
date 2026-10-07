package com.bibireden.playerex

import com.bibireden.playerex.compat.Compatibility
import com.bibireden.playerex.progression.PlayerProgression
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory

object PlayerEX {
    const val MOD_ID = "playerex"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    @JvmStatic
    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)

    fun init() {
        PlayerProgression.install()
        Compatibility.logDetectedIntegrations()
        LOGGER.info("PlayerEx 1.21.1 common foundation initialized")
    }
}
