package com.bibireden.playerex.item

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.config.PlayerExConfigState
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import com.bibireden.playerex.api.PlayerEXTags
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.*
import java.util.function.BiConsumer

object ItemProgression {
    private val weapons = PlayerEXTags.WEAPONS
    private val armor = PlayerEXTags.ARMOR
    private val armorBlacklist = PlayerEXTags.ARMOR_BLACKLIST
    private val unbreakable = PlayerEXTags.UNBREAKABLE_ITEMS
    private val bosses = TagKey.create(Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "bosses"))
    private val rangedDamage = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ranged_weapon", "damage")
    private val weaponBonus = PlayerEX.id("item/weapon_level")
    private val armorBonus = EquipmentSlot.values().associateWith { PlayerEX.id("item/armor_level/${it.serializedName}") }

    @JvmStatic fun isWeapon(stack: ItemStack): Boolean = !stack.isEmpty && (stack.item is SwordItem || stack.item is BowItem || stack.item is CrossbowItem || stack.`is`(weapons))
    @JvmStatic fun isArmor(stack: ItemStack): Boolean = !stack.isEmpty && (stack.item is ArmorItem || stack.`is`(armor)) && !stack.`is`(armorBlacklist)
    @JvmStatic fun state(stack: ItemStack): ItemProgressionState = stack.getOrDefault(PlayerExItemComponents.PROGRESSION, ItemProgressionState.EMPTY)
    @JvmStatic fun isBroken(stack: ItemStack): Boolean = PlayerExConfigState.current().itemBreakingEnabled && state(stack).broken

    @JvmStatic
    fun onKilled(victim: LivingEntity, player: ServerPlayer) {
        if (victim === player || victim.level().isClientSide) return
        val config = PlayerExConfigState.current()
        fun reward(weapon: Boolean): Int {
            val shared: Int
            val overrideXp: Int
            when {
                victim.type.`is`(bosses) || victim.type == net.minecraft.world.entity.EntityType.WITHER || victim.type == net.minecraft.world.entity.EntityType.ENDER_DRAGON -> {
                    shared = config.itemXpFromBoss; overrideXp = if (weapon) config.weaponXpFromBoss else config.armorXpFromBoss
                }
                victim.type.category == MobCategory.MONSTER -> {
                    shared = config.itemXpFromHostile; overrideXp = if (weapon) config.weaponXpFromHostile else config.armorXpFromHostile
                }
                victim.type.category.isFriendly -> {
                    shared = config.itemXpFromPassive; overrideXp = if (weapon) config.weaponXpFromPassive else config.armorXpFromPassive
                }
                else -> return 0
            }
            return if (overrideXp >= 0) overrideXp else shared
        }
        award(player.mainHandItem, reward(true))
        val armorXp = reward(false)
        for (stack in player.armorSlots) award(stack, armorXp)
    }

    @JvmStatic
    fun award(stack: ItemStack, xp: Int) {
        val config = PlayerExConfigState.current()
        val weapon = isWeapon(stack)
        if (xp <= 0 || isBroken(stack) || (!weapon && !isArmor(stack)) ||
            (weapon && !config.weaponLevelingEnabled) || (!weapon && !config.armorLevelingEnabled)) return
        val current = state(stack)
        val maxLevel = if (weapon) config.weaponMaxLevel else config.armorMaxLevel
        if (current.level >= maxLevel) return
        var level = current.level
        var experience = (current.experience.toLong() + xp).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val formulas = PlayerExConfigState.formulaEngine()
        while (level < maxLevel) {
            val cost = (if (weapon) formulas.weaponCost((level + 1).toDouble()) else formulas.armorCost((level + 1).toDouble())).coerceAtLeast(1)
            if (experience < cost) break
            experience -= cost
            level++
        }
        stack.set(PlayerExItemComponents.PROGRESSION, current.copy(level = level, experience = experience))
    }

    @JvmStatic
    fun preserveBroken(stack: ItemStack): Boolean {
        val config = PlayerExConfigState.current()
        if (!config.itemBreakingEnabled || !stack.`is`(unbreakable)) return false
        if (config.destroyCurseOfBinding && isArmor(stack) && net.minecraft.world.item.enchantment.EnchantmentHelper.has(stack, net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) return false
        val current = state(stack)
        if (current.broken) return true
        if (!config.infiniteItemBreaking && current.timesBroken >= config.timesItemCanBreak) return false
        val breaks = if (config.infiniteItemBreaking) current.timesBroken else current.timesBroken + 1
        stack.set(PlayerExItemComponents.PROGRESSION, current.copy(timesBroken = breaks, broken = true))
        return true
    }

    @JvmStatic
    fun notifyBroken(stack: ItemStack, entity: LivingEntity?) {
        if (entity !is ServerPlayer || !PlayerExConfigState.current().messageOnItemBreak) return
        entity.sendSystemMessage(Component.translatable(if (isArmor(stack)) "playerex.armor.broke" else "playerex.item.broke", stack.displayName).withStyle(net.minecraft.ChatFormatting.RED))
    }

    @JvmStatic
    fun repair(stack: ItemStack, damage: Int) {
        val current = state(stack)
        if (current.broken && damage < stack.damageValue) stack.set(PlayerExItemComponents.PROGRESSION, current.copy(broken = false))
    }

    @JvmStatic
    fun filterModifier(stack: ItemStack, attribute: Holder<Attribute>, modifier: AttributeModifier, consumer: BiConsumer<Holder<Attribute>, AttributeModifier>) {
        if (isBroken(stack) && (attribute == Attributes.ARMOR || attribute == Attributes.ARMOR_TOUGHNESS ||
                attribute == Attributes.KNOCKBACK_RESISTANCE || attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED)) return
        consumer.accept(attribute, modifier)
    }

    @JvmStatic
    fun addModifiers(stack: ItemStack, slot: EquipmentSlot, consumer: BiConsumer<Holder<Attribute>, AttributeModifier>) {
        if (isBroken(stack)) return
        val config = PlayerExConfigState.current()
        val level = state(stack).level
        if (level <= 0) return
        if (slot == EquipmentSlot.MAINHAND && config.weaponLevelingEnabled && isWeapon(stack)) {
            if (stack.item is BowItem || stack.item is CrossbowItem) {
                val ranged = BuiltInRegistries.ATTRIBUTE.get(rangedDamage)
                if (ranged != null) consumer.accept(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(ranged), AttributeModifier(weaponBonus, level * config.weaponDamagePerLevel, AttributeModifier.Operation.ADD_VALUE))
            } else consumer.accept(Attributes.ATTACK_DAMAGE, AttributeModifier(weaponBonus, level * config.weaponDamagePerLevel, AttributeModifier.Operation.ADD_VALUE))
        }
        if (config.armorLevelingEnabled && isArmor(stack) && Equipable.get(stack)?.equipmentSlot == slot) {
            consumer.accept(Attributes.ARMOR, AttributeModifier(armorBonus.getValue(slot), level * config.armorPerLevel, AttributeModifier.Operation.ADD_VALUE))
        }
    }

    @JvmStatic
    fun armorReduction(entity: LivingEntity, damage: Float): Float {
        val config = PlayerExConfigState.current()
        if (!config.armorLevelingEnabled || entity.level().isClientSide) return damage
        val percent = entity.armorSlots.sumOf { stack ->
            if (!isArmor(stack) || isBroken(stack)) 0.0 else (state(stack).level * config.armorReductionPerLevel).coerceAtMost(config.armorMaxReduction)
        } / 400.0
        return (damage * (1.0 - percent.coerceIn(0.0, 1.0))).toFloat()
    }

    @JvmStatic
    @JvmOverloads
    fun tooltip(stack: ItemStack, lines: MutableList<Component>, advanced: Boolean = false, expanded: Boolean = true) {
        ItemProgressionTooltip.append(stack, lines, advanced, expanded)
    }
}
