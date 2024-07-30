package com.bibireden.playerex

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.api.event.PlayerEXSoundEvents
import com.bibireden.playerex.networking.NetworkingChannels
import com.bibireden.playerex.networking.NetworkingPackets
import com.bibireden.playerex.networking.registerClientbound
import com.bibireden.playerex.networking.types.NotificationType
import com.bibireden.playerex.registry.PlayerEXMenuRegistry
import com.bibireden.playerex.ui.PlayerEXScreen
import com.bibireden.playerex.ui.menus.PlayerEXAttributesMenu
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.sounds.SoundSource
import org.lwjgl.glfw.GLFW

object PlayerEXClient : ClientModInitializer {
	val MAIN_UI_SCREEN_ID = PlayerEX.id("main_ui_model")

	val KEYBINDING_MAIN_SCREEN: KeyMapping = KeyBindingHelper.registerKeyBinding(KeyMapping("${PlayerEX.MOD_ID}.key.main_screen", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, "key.categories.${PlayerEX.MOD_ID}"))

	override fun onInitializeClient() {
		NetworkingChannels.NOTIFICATIONS.registerClientbound(NetworkingPackets.Notify::class) { (type), ctx ->
			val soundSettings = PlayerEX.CONFIG.soundSettings
			when (type) {
				NotificationType.LevelUpAvailable -> ctx.player().playNotifySound(PlayerEXSoundEvents.LEVEL_UP_SOUND, SoundSource.NEUTRAL, soundSettings.levelUpVolume.toFloat(), 1F)
				NotificationType.Spent -> ctx.player().playNotifySound(PlayerEXSoundEvents.SPEND_SOUND, SoundSource.NEUTRAL, soundSettings.skillUpVolume.toFloat(), 1F)
				NotificationType.Refunded -> ctx.player().playNotifySound(PlayerEXSoundEvents.REFUND_SOUND, SoundSource.NEUTRAL, soundSettings.refundVolume.toFloat(), 0.7F)
			}
		}

		EntityAttributeModifiedEvents.MODIFIED.register { attribute, entity, _, _, _ ->
			if (entity is LocalPlayer) {
				val screen = Minecraft.getInstance().screen
				if (screen is PlayerEXScreen) {
					if (attribute == PlayerEXAttributes.LEVEL) {
						DataAttributesAPI.getValue(attribute, entity).map(Double::toInt).ifPresent(screen::onLevelUpdated)
					}
					else {
						DataAttributesAPI.getValue(attribute, entity).ifPresent { value ->
							screen.onAttributeUpdated(attribute, value)
						}
					}
				}
			}
		}

		PlayerEXMenuRegistry.register(PlayerEXAttributesMenu::class.java)

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (PlayerEX.CONFIG.disableUI) return@register
			while (KEYBINDING_MAIN_SCREEN.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(PlayerEXScreen())
				}
			}
		}
	}
}