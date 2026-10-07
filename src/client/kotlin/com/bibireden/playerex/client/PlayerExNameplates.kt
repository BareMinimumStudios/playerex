package com.bibireden.playerex.client
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object PlayerExNameplates {
    private var enabled = true
    private var color = 0xFFAA00
    private val attribute by lazy { BuiltInRegistries.ATTRIBUTE.getHolder(PlayerEXAttributes.LEVEL_ID).orElse(null) }
    fun configure(show: Boolean, rgb: Int) { enabled = show; color = rgb and 0xFFFFFF }
    @JvmStatic fun decorate(text: Component, entity: Entity): Component {
        if (!enabled || entity !is Player) return text
        val holder = attribute ?: return text
        val level = entity.getAttribute(holder)?.value ?: return text
        if (!level.isFinite()) return text
        return text.copy().append(" ").append(Component.translatable("playerex.ui.nameplate.level", level.toInt()).withStyle { it.withColor(color) })
    }
}
