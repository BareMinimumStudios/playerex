package com.bibireden.playerex.client

import com.bibireden.playerex.api.event.PlayerEXSoundEvents
import com.bibireden.playerex.networking.ClientNotificationBus
import com.bibireden.playerex.networking.type.NotificationType
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import org.lwjgl.glfw.GLFW

object PlayerExClient {
    val openKey = KeyMapping("playerex.key.main_screen", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "key.categories.playerex")

    private var initialized = false
    fun init() {
        if (initialized) return
        initialized = true
        ClientNotificationBus.listen { notification ->
            val client = Minecraft.getInstance()
            client.execute {
                if (client.player != null) {
                    val sound = when (notification) {
                        NotificationType.LEVEL_UP_AVAILABLE -> PlayerEXSoundEvents.LEVEL_UP_SOUND
                        NotificationType.SPENT -> PlayerEXSoundEvents.SPEND_SOUND
                        NotificationType.REFUNDED -> PlayerEXSoundEvents.REFUND_SOUND
                    }
                    val volume = PlayerExSoundSettings.volume(notification)
                    if (volume > 0f) client.soundManager.play(SimpleSoundInstance.forUI(sound, if (notification == NotificationType.REFUNDED) 0.7f else 1f, volume))
                }
            }
        }
    }

    fun tick() {
        ClientAttributeSync.tick()
        while (openKey.consumeClick()) {
            val client = Minecraft.getInstance()
            if (client.player != null && client.screen == null) PlayerExScreen.open()
        }
    }
}
