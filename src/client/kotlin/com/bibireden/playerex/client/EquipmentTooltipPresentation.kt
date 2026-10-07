package com.bibireden.playerex.client

import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.component.ItemAttributeModifiers
import java.util.function.Consumer

/** Client presentation only; never modifies item or player attributes. */
object EquipmentTooltipPresentation {
    enum class Mode { Default, Vanilla, PlayerEX }
    @JvmStatic var holdShift = false
    private val weaponLevel = com.bibireden.playerex.PlayerEX.id("item/weapon_level")
    private var mode = Mode.Vanilla
    fun configure(candidate: Mode) { mode = candidate }
    @JvmStatic fun modifiers(source: List<Pair<Holder<Attribute>, AttributeModifier>>,
        consumer: java.util.function.BiConsumer<Holder<Attribute>, AttributeModifier>) {
        val rows = source.toMutableList()
        var i = 0
        while (i < rows.size) {
            val (attribute, bonus) = rows[i]
            val progression = bonus.id().namespace == "playerex" &&
                (bonus.id().path == "item/weapon_level" || bonus.id().path.startsWith("item/armor_level/"))
            if (progression && bonus.operation() == AttributeModifier.Operation.ADD_VALUE) {
                val target = rows.indexOfFirst { (a, m) -> a == attribute && m.id() != bonus.id() && m.operation() == AttributeModifier.Operation.ADD_VALUE }
                if (target >= 0) {
                    val base = rows[target].second
                    rows[target] = attribute to AttributeModifier(base.id(), base.amount() + bonus.amount(), base.operation())
                    rows.removeAt(i)
                    continue
                }
            }
            i++
        }
        rows.forEach { (attribute, modifier) -> consumer.accept(attribute, modifier) }
    }
    @JvmStatic fun append(consumer: Consumer<Component>, player: Player?, attribute: Holder<Attribute>, modifier: AttributeModifier): Boolean {
        if (mode == Mode.Default || player == null || modifier.operation() != AttributeModifier.Operation.ADD_VALUE) return false
        if (!modifier.`is`(Item.BASE_ATTACK_DAMAGE_ID) && !modifier.`is`(Item.BASE_ATTACK_SPEED_ID)) return false
        val instance = player.getAttribute(attribute) ?: return false
        val amount = when (mode) {
            Mode.PlayerEX -> instance.value + modifier.amount() - (instance.getModifier(modifier.id())?.amount() ?: 0.0) -
                (if (modifier.`is`(Item.BASE_ATTACK_DAMAGE_ID)) instance.getModifier(weaponLevel)?.amount() ?: 0.0 else 0.0)
            else -> modifier.amount()
        }
        if (!amount.isFinite()) return false
        val equals = mode == Mode.PlayerEX
        if (equals || amount != 0.0) {
            val kind = if (equals) "equals" else if (amount > 0) "plus" else "take"
            val line = Component.translatable("attribute.modifier.$kind.0",
                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(if (equals) amount else kotlin.math.abs(amount)),
                Component.translatable(attribute.value().descriptionId))
            consumer.accept(if (equals) Component.literal(" ").append(line).withStyle(ChatFormatting.DARK_GREEN)
                else line.withStyle(attribute.value().getStyle(amount > 0)))
        }
        return true
    }
}
