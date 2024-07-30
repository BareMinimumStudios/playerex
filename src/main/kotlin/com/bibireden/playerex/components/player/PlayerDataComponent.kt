package com.bibireden.playerex.components.player

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.IEntityAttribute
import com.bibireden.data_attributes.api.attribute.IEntityAttributeInstance
import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.endec.nbt.NbtDeserializer
import com.bibireden.data_attributes.endec.nbt.NbtSerializer
import com.bibireden.playerex.PlayerEX.CONFIG
import com.bibireden.playerex.api.PlayerEXModifiers
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.components.PlayerEXComponents
import com.bibireden.playerex.ext.id
import com.bibireden.playerex.factory.ServerNetworkingFactory
import com.bibireden.playerex.networking.NetworkingChannels
import com.bibireden.playerex.networking.NetworkingPackets
import com.bibireden.playerex.networking.types.NotificationType
import com.bibireden.playerex.registry.RefundConditionRegistry
import com.bibireden.playerex.util.PlayerEXUtil
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.player.Player
import kotlin.math.round

/**
 * Implementation of player-data that is meant to be synchronized. Contains [Attribute] data and provided modifiers,
 * skill points and refundable points.
 */
class PlayerDataComponent(
    private val player: Player,
    private var _refundablePoints: Int = 0,
    private var _skillPoints: Int = 0,
    private var _modifiers: MutableMap<ResourceLocation, Double> = mutableMapOf(),
    var isLevelUpNotified: Boolean = false,
) : IPlayerDataComponent, AutoSyncedComponent {
    data class Packet(val modifiers: Map<ResourceLocation, Double>, val refundablePoints: Int, val skillPoints: Int, val isLevelUpNotified: Boolean)

    private fun toPacketNbt(): CompoundTag = CompoundTag().also(::writeToNbt)

    companion object {
        val ENDEC = StructEndecBuilder.of(
            Endec.map(Endecs.IDENTIFIER, Endec.DOUBLE).fieldOf("modifiers") { it.modifiers },
            Endec.INT.fieldOf("refundablePoints") { it.refundablePoints },
            Endec.INT.fieldOf("skillPoints") { it.skillPoints },
            Endec.BOOLEAN.fieldOf("isLevelUpNotified") { it.isLevelUpNotified },
            ::Packet
        )
    }

    /**
     * Attempts to fetch an [AttributeInstance] based on the provided [ResourceLocation].
     *
     * @return a [Pair] of the instance, and whether that instance contains an existing modifier that has the UUID [PlayerEXModifiers.UUID].
     */
    private fun getInstance(key: ResourceLocation): Pair<AttributeInstance, Boolean>? {
        val attribute = BuiltInRegistries.ATTRIBUTE[key] ?: return null
        val instance = this.player.attributes.getInstance(attribute) ?: return null
        return Pair(instance, instance.getModifier(PlayerEXModifiers.UUID) != null)
    }

    private fun sync(packet: ComponentPacketWriter) {
        this.player.level().takeUnless { it.isClientSide() }?.let { world ->
            PlayerEXComponents.PLAYER_DATA.sync(this.player, packet)
        }
    }

    /**
     * Attempts to set a special PlayerEX-specific modifier to an [AttributeInstance] based on the [ResourceLocation] key provided.
     */
    private fun trySet(key: ResourceLocation, value: Double): Boolean {
        val (instance, isPlayerEXModifierPresent) = this.getInstance(key) ?: return false
        if (isPlayerEXModifierPresent) {
            (instance as IEntityAttributeInstance).updateModifier(PlayerEXModifiers.UUID, value)
        }
        else {
            instance.addPermanentModifier(AttributeModifier(PlayerEXModifiers.UUID, "PlayerEX Attribute", value, AttributeModifier.Operation.ADDITION))
        }
        this._modifiers[key] = value
        return true
    }

    // todo: this function angers me!!!! 😡 remove should be suitable enough instead of a try.
    /**
     * Attempts to remove a special PlayerEX modifier if present on the [AttributeInstance] linked by the provided [ResourceLocation] key.
     *
     * @return [Boolean] Whether there was an existing [AttributeInstance], whether an [AttributeInstance] was removed or not.
     * */
    private fun tryRemove(key: ResourceLocation): Boolean {
        return this.getInstance(key)?.let { (instance, isModifierPresent) ->
            if (isModifierPresent) {
                instance.removeModifier(PlayerEXModifiers.UUID)
            }
        } != null
    }

    override fun get(attribute: Attribute): Double {
        return this._modifiers.getOrDefault(attribute.id, 0.0)
    }

    override fun set(attribute: Attribute, value: Int) {
        val identifier = attribute.id
        val attributeValue = attribute.sanitizeValue(value.toDouble())

        if (!this.trySet(identifier, attributeValue)) return

        this.sync { buf, player -> buf.writeNbt(toPacketNbt())}
    }

    override fun add(attribute: Attribute, value: Double) {
        this.set(attribute, (value + this.get(attribute)).toInt())
    }

    override fun remove(attribute: Attribute) {
        val identifier = attribute.id
        if (!this.tryRemove(identifier).also { if (it) this._modifiers.remove(identifier) }) return
        this.sync { buf, player -> buf.writeNbt(toPacketNbt())}
    }

    override fun reset(percent: Int) {
        val partition = if (percent == 0) 0.0 else percent / 100.0

        val kept = mutableMapOf<ResourceLocation, Double>()
        for ((id, value) in this.modifiers) {
            if (partition == 0.0) {
                this.tryRemove(id)
            }
            else {
                val retained = value * partition
                if (!this.trySet(id, retained)) continue
                kept[id] = retained
            }
        }

        this._modifiers = kept
        this._refundablePoints = round(this._refundablePoints * partition).toInt()
        this._skillPoints = round(this._skillPoints * partition).toInt()
        this.isLevelUpNotified = false

        this.sync { buf, _ -> buf.writeNbt(toPacketNbt())}
    }

    override fun addSkillPoints(points: Int) {
        this._skillPoints += points
        this.sync { buf, player -> buf.writeNbt(toPacketNbt())}
    }

    override fun addRefundablePoints(points: Int): Int {
        val previous = this.refundablePoints
        var maxRefundPoints = 0.0

        for (condition in RefundConditionRegistry.get()) {
            maxRefundPoints += condition(this, this.player)
        }

        this._refundablePoints = round(Mth.clamp((this.refundablePoints + points).toDouble(), 0.0, maxRefundPoints)).toInt()

        this.sync { buf, _ -> buf.writeNbt(toPacketNbt())}

        return this.refundablePoints - previous
    }

    override fun levelUp(amount: Int, override: Boolean): Boolean {
        if (amount <= 0) return false

        return DataAttributesAPI.getValue(PlayerEXAttributes.LEVEL, player).map { level ->
            val expectedLevel = level + amount
            // get the expected level, but do not go beyond the bounds of the maximum!
            if (expectedLevel > PlayerEXAttributes.LEVEL.maxValue) return@map false

            val required = PlayerEXUtil.getRequiredXpForLevel(player, expectedLevel)

            val isEnoughExperience = player.experienceLevel >= required || override
            if (isEnoughExperience) {
                val skillPoints = CONFIG.skillPointsPerLevelUp * amount
                val component = PlayerEXComponents.PLAYER_DATA.get(player)

                if (!override) player.giveExperienceLevels(-required)
                component.addSkillPoints(skillPoints)
                component.set(PlayerEXAttributes.LEVEL, expectedLevel.toInt())

                ServerNetworkingFactory.notify(player, NotificationType.Spent)
            }
            return@map isEnoughExperience
        }.orElse(false)
    }

    override fun skillUp(skill: Attribute, amount: Int, override: Boolean): Boolean {
        if (amount <= 0) return false

        return DataAttributesAPI.getValue(skill, player).map { current ->
            val expected = current + amount
            // too high
            if (expected > (skill as IEntityAttribute).`data_attributes$max`()) return@map false
            if (!override) {
                // not enough skill points
                if (skillPoints < amount) return@map false
                this._skillPoints -= amount
            }
            this.set(skill, expected.toInt())
            // signal to a client that an increase has occurred...
            NetworkingChannels.NOTIFICATIONS.serverHandle(player).send(NetworkingPackets.Notify(NotificationType.Spent))
            return@map true
        }.orElse(false)
    }

    override fun refund(skill: Attribute, amount: Int): Boolean {
        if (amount <= 0 || this.refundablePoints < amount) return false
        return DataAttributesAPI.getValue(skill, player).map { value ->
            if (amount > value) return@map false

            this.addRefundablePoints(-amount)
            this.addSkillPoints(amount)
            this.set(skill, (value - amount).toInt())

            ServerNetworkingFactory.notify(player, NotificationType.Refunded)

            true
        }.orElse(false)
    }

    val modifiers: Map<ResourceLocation, Double>
        get() = this._modifiers

    override val skillPoints: Int
        get() = this._skillPoints

    override val refundablePoints: Int
        get() = this._refundablePoints

    override fun readFromNbt(tag: CompoundTag) {
        ENDEC.decodeFully(NbtDeserializer::of, tag.get("DART")).also {
            this._modifiers = it.modifiers.toMutableMap()
            this._refundablePoints = it.refundablePoints
            this._skillPoints = it.skillPoints
            this.isLevelUpNotified = it.isLevelUpNotified
        }
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.put("DART", ENDEC.encodeFully(NbtSerializer::of, Packet(this._modifiers, this.refundablePoints, this.skillPoints, this.isLevelUpNotified)))
    }

    override fun shouldSyncWith(player: ServerPlayer): Boolean = player == this.player

    override fun applySyncPacket(buf: FriendlyByteBuf) {
        this.readFromNbt(buf.readNbt() ?: return)
    }
}