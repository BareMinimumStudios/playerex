package com.bibireden.playerex

import com.bibireden.data_attributes.api.attribute.EntityAttributeSupplier
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents
import com.bibireden.data_attributes.api.factory.DefaultAttributeFactory
import com.bibireden.playerex.api.PlayerEXAPI
import com.bibireden.playerex.api.attribute.DefaultAttributeImpl
import com.bibireden.playerex.api.event.LivingEntityEvents
import com.bibireden.playerex.api.event.PlayerEXSoundEvents
import com.bibireden.playerex.api.event.PlayerEntityEvents
import com.bibireden.playerex.components.PlayerEXComponents
import com.bibireden.playerex.config.PlayerEXConfig
import com.bibireden.playerex.factory.*
import com.bibireden.playerex.networking.NetworkingChannels
import com.bibireden.playerex.networking.NetworkingPackets
import com.bibireden.playerex.networking.registerServerbound
import com.bibireden.playerex.networking.types.UpdatePacketType
import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes
import eu.pb4.placeholders.api.Placeholders
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.Attributes
import org.slf4j.LoggerFactory

object PlayerEX : ModInitializer {
	const val MOD_ID: String = "playerex"

	@JvmField
	val LOGGER = LoggerFactory.getLogger(MOD_ID)

	@JvmField
	val CONFIG = PlayerEXConfig.createAndLoad()

	fun id(path: String) = ResourceLocation.tryBuild(MOD_ID, path)!!

	private val gimmick = listOf(
		"Let's do it right this time...",
		"PlayerEX: Bringing leveling and attribute manipulation to your doorstep."
	).random()

	override fun onInitialize() {
		NetworkingChannels.NOTIFICATIONS.registerClientboundDeferred(NetworkingPackets.Notify::class.java)

		NetworkingChannels.MODIFY.registerServerbound(NetworkingPackets.Update::class) { (type, id, amount), ctx ->
			EntityAttributeSupplier(id).get().ifPresent {
				when (type) {
					UpdatePacketType.Skill -> PlayerEXComponents.PLAYER_DATA.get(ctx.player).skillUp(it, amount)
					UpdatePacketType.Refund -> PlayerEXComponents.PLAYER_DATA.get(ctx.player).refund(it, amount)
				}
			}
		}
		NetworkingChannels.MODIFY.registerServerbound(NetworkingPackets.Level::class) { (amount), ctx ->
			PlayerEXComponents.PLAYER_DATA.get(ctx.player).levelUp(amount)
		}

		CommandRegistrationCallback.EVENT.register(PlayerEXCommands::register)

		ServerLoginConnectionEvents.QUERY_START.register(ServerNetworkingFactory::onLoginQueryStart)
		ServerPlayerEvents.COPY_FROM.register(EventFactory::reset)

		LivingEntityEvents.ON_HEAL.register(EventFactory::healed)
		LivingEntityEvents.ON_TICK.register(EventFactory::healthRegeneration)
		LivingEntityEvents.ON_DAMAGE.register(EventFactory::onDamage)
		LivingEntityEvents.SHOULD_DAMAGE.register(EventFactory::shouldDamage)

		PlayerEntityEvents.ON_CRITICAL.register(EventFactory::onCritAttack)
		PlayerEntityEvents.SHOULD_CRITICAL.register(EventFactory::attackIsCrit)

		DamageFactory.forEach(PlayerEXAPI::registerDamageModification)
		RefundFactory.forEach(PlayerEXAPI::registerRefundCondition)

		PlaceholderFactory.STORE.forEach(Placeholders::register)

		Registry.register(BuiltInRegistries.SOUND_EVENT, PlayerEXSoundEvents.LEVEL_UP_SOUND.location, PlayerEXSoundEvents.LEVEL_UP_SOUND)
		Registry.register(BuiltInRegistries.SOUND_EVENT, PlayerEXSoundEvents.SPEND_SOUND.location, PlayerEXSoundEvents.SPEND_SOUND)
		Registry.register(BuiltInRegistries.SOUND_EVENT, PlayerEXSoundEvents.REFUND_SOUND.location, PlayerEXSoundEvents.REFUND_SOUND)

		DefaultAttributeFactory.registerOverrides(DefaultAttributeImpl.OVERRIDES)
		DefaultAttributeFactory.registerFunctions(DefaultAttributeImpl.FUNCTIONS)
		DefaultAttributeFactory.registerEntityTypes(DefaultAttributeImpl.ENTITY_TYPES)

		EntityAttributeModifiedEvents.MODIFIED.register { attribute, entity, _, _, _ ->
			if (entity?.level() == null) return@register // no entity & no world, skip

			if (!entity.level().isClientSide()) {
				if (attribute == Attributes.MAX_HEALTH) {
					entity.health = attribute.sanitizeValue(entity.health.toDouble()).toFloat()
				}
				else if (attribute == AdditionalEntityAttributes.LUNG_CAPACITY) {
					entity.airSupply = attribute.sanitizeValue(entity.airSupply.toDouble()).toInt()
				}
			}
		}

		LOGGER.info(gimmick)
	}
}