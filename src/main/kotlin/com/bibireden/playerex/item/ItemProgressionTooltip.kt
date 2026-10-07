package com.bibireden.playerex.item

import com.bibireden.playerex.config.PlayerExConfigState
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.ItemStack
import java.util.Locale

/** Presentation of existing server-owned item components; no gameplay mutation. */
object ItemProgressionTooltip {
    data class Colors(val title: Int = 0xE3C46D, val arrow: Int = 0xE3C46D, val text: Int = 0xAAA99F,
        val values: Int = 0xE8E5DA, val shift: Int = 0xE3C46D)
    private var colors = Colors()
    fun configure(candidate: Colors) { colors = candidate }
    private fun colored(key: String, color: Int, vararg args: Any): Component =
        Component.translatable(key, *args).withStyle(Style.EMPTY.withColor(color and 0xFFFFFF))
    fun append(stack: ItemStack, lines: MutableList<Component>, advanced: Boolean, expanded: Boolean) {
        if (lines.isEmpty() || stack.has(DataComponents.HIDE_TOOLTIP) || stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) return
        val config = PlayerExConfigState.current()
        val state = ItemProgression.state(stack)
        val weapon = ItemProgression.isWeapon(stack)
        val levelable = (weapon && config.weaponLevelingEnabled) || (ItemProgression.isArmor(stack) && config.armorLevelingEnabled)
        val additions = mutableListOf<Component>()
        if (ItemProgression.isBroken(stack)) {
            additions += (if (config.infiniteItemBreaking) Component.translatable("playerex.broken")
                else Component.translatable("playerex.broken.numbered", state.timesBroken, config.timesItemCanBreak))
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            additions += colored("playerex.item.repair", colors.text)
            additions += Component.empty()
        }
        if (levelable) {
            if (!expanded) additions += colored("playerex.item.press_shift", colors.shift)
            else {
                additions += colored("playerex.item.level_title", colors.title)
                fun row(key: String, value: Component): Component = Component.literal(" ▶ ")
                    .withStyle(Style.EMPTY.withColor(colors.arrow and 0xFFFFFF))
                    .append(colored(key, colors.text)).append(value)
                fun value(text: String) = Component.literal(text).withStyle(Style.EMPTY.withColor(colors.values and 0xFFFFFF))
                val max = if (weapon) config.weaponMaxLevel else config.armorMaxLevel
                additions += row("playerex.item.level_label", value(state.level.toString()))
                if (state.level < max) {
                    val formulas = PlayerExConfigState.formulaEngine()
                    val cost = (if (weapon) formulas.weaponCost((state.level + 1).toDouble())
                        else formulas.armorCost((state.level + 1).toDouble())).coerceAtLeast(1)
                    val progress = if (advanced) value(state.experience.toString()).append(Component.literal("/")
                        .withStyle(Style.EMPTY.withColor(colors.text and 0xFFFFFF))).append(value(cost.toString()))
                        else value(String.format(Locale.ROOT, "%.2f%%", state.experience.toDouble() / cost * 100))
                    additions += row("playerex.item.progress_label", progress)
                } else additions += Component.literal(" ▶ ").withStyle(Style.EMPTY.withColor(colors.arrow and 0xFFFFFF))
                    .append(colored(if (state.level == max) "playerex.item.max_level" else "playerex.item.over_max_level", colors.values))
                if (ItemProgression.isArmor(stack)) {
                    val reduction = (state.level * config.armorReductionPerLevel).coerceAtMost(config.armorMaxReduction)
                    if (reduction > 0) additions += row("playerex.item.reduction_label", value(String.format(Locale.ROOT, "%.2f%%", reduction)))
                }
            }
        }
        lines.addAll(1, additions)
    }
}
