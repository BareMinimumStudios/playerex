package com.bibireden.playerex.item

import com.bibireden.playerex.PlayerEX
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.*
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.Level
import java.util.function.BiConsumer

object RelicEquipment {
    val names = listOf("common", "uncommon", "rare", "epic", "mythical", "legendary", "immortal")
    private val colors = listOf(ChatFormatting.WHITE, ChatFormatting.GREEN, ChatFormatting.BLUE,
        ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.AQUA)
    val factories: Map<String, () -> Item> = linkedMapOf(
        "ring_relic" to { Accessory() }, "amulet_relic" to { Accessory() },
        "head_relic" to { Armor(ArmorItem.Type.HELMET) }, "chest_relic" to { Armor(ArmorItem.Type.CHESTPLATE) }
    )
    @JvmStatic fun initialize(stack: ItemStack, entity: LivingEntity) {
        if (entity.level().isClientSide || stack.has(PlayerExItemComponents.RELIC)) return
        val state = RelicWeights.roll(entity.random) ?: return
        stack.set(PlayerExItemComponents.RELIC, state)
    }
    @JvmStatic fun rarityColor(stack: ItemStack): ChatFormatting? = stack.get(PlayerExItemComponents.RELIC)?.let { colors[it.rarity] }
    @JvmStatic fun tooltip(stack: ItemStack, lines: MutableList<Component>) {
        val state = stack.get(PlayerExItemComponents.RELIC) ?: return
        lines += Component.translatable("rareness.playerex.${names[state.rarity]}").withStyle(colors[state.rarity])
    }
    @JvmStatic fun modifiers(stack: ItemStack, slot: String): List<Pair<Holder<Attribute>, AttributeModifier>> {
        if (ItemProgression.isBroken(stack)) return emptyList()
        val state = stack.get(PlayerExItemComponents.RELIC) ?: return emptyList()
        return state.attributes.mapNotNull { value ->
            val holder = BuiltInRegistries.ATTRIBUTE.getHolder(value.attribute).orElse(null) ?: return@mapNotNull null
            holder to AttributeModifier(PlayerEX.id("relic/$slot/${value.attribute.namespace}/${value.attribute.path}"), value.amount,
                if (value.multiply) AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL else AttributeModifier.Operation.ADD_VALUE)
        }
    }
    @JvmStatic fun mergeArmor(stack: ItemStack, slot: EquipmentSlot, base: List<Pair<Holder<Attribute>, AttributeModifier>>,
                              consumer: BiConsumer<Holder<Attribute>, AttributeModifier>) {
        if ((stack.item as? ArmorItem)?.equipmentSlot != slot) { base.forEach { consumer.accept(it.first, it.second) }; return }
        val merged = base.toMutableList()
        for ((attribute, modifier) in modifiers(stack, slot.serializedName)) {
            var index = -1
            for (i in merged.indices) {
                val candidate = merged[i]
                if (candidate.first == attribute && candidate.second.operation() == modifier.operation() &&
                    (index < 0 || candidate.second.amount() > merged[index].second.amount())) index = i
            }
            if (index < 0) merged += attribute to modifier
            else if (modifier.amount() > merged[index].second.amount()) {
                val original = merged[index].second
                merged[index] = attribute to AttributeModifier(original.id(), modifier.amount(), original.operation())
            }
        }
        merged.forEach { consumer.accept(it.first, it.second) }
    }
    private class Accessory : Item(Properties().stacksTo(1)) {
        override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slot: Int, selected: Boolean) {
            if (entity is LivingEntity) initialize(stack, entity)
        }
        override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
            RelicEquipment.tooltip(stack, tooltip)
        }
    }
    class Armor(type: ArmorItem.Type) : ArmorItem(ArmorMaterials.CHAIN, type, Properties().stacksTo(1).durability(type.getDurability(15))) {
        override fun getDefaultAttributeModifiers(): ItemAttributeModifiers = ItemAttributeModifiers.EMPTY
        override fun getEnchantmentValue(): Int = ArmorMaterials.GOLD.value().enchantmentValue()
        override fun isValidRepairItem(stack: ItemStack, ingredient: ItemStack): Boolean = ingredient.`is`(BuiltInRegistries.ITEM.get(PlayerEX.id("relic_shard")))
        override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slot: Int, selected: Boolean) {
            if (entity is LivingEntity) initialize(stack, entity)
        }
        override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
            RelicEquipment.tooltip(stack, tooltip)
        }
    }
}
