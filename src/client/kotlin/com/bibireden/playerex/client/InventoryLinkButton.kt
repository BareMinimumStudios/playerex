package com.bibireden.playerex.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.util.function.IntSupplier

/** Normal inventory entry tab; the visible face is clickable, its decorative rail is not. */
class InventoryLinkButton(private val tabX: IntSupplier, private val tabY: IntSupplier) :
    Button(tabX.asInt + 2, tabY.asInt + 18, 12, 21,
        Component.translatable("playerex.screen.title"), OnPress { PlayerExScreen.open() }, DEFAULT_NARRATION) {
    init { tooltip = Tooltip.create(Component.translatable("playerex.screen.title")) }

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (com.bibireden.playerex.config.PlayerExConfigState.current().disableUI) { isHovered = false; return }
        // The recipe book changes the panel position without rebuilding its widgets.
        val anchorX = tabX.asInt; val anchorY = tabY.asInt
        x = anchorX + 2; y = anchorY + 18
        isHovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height
        g.blit(TAB, anchorX, anchorY, 19, 55, 0f, 0f, 19, 55, 19, 55)
        g.pose().pushPose(); g.pose().translate(x.toFloat(), y.toFloat(), 0f)
        drawIcon(g, width, height)
        g.pose().popPose()
    }

    companion object {
        private val TAB = ResourceLocation.parse("playerex:textures/gui/poke/right_tab.png")
        private val PENCIL = ResourceLocation.parse("playerex:textures/gui/poke/icon_pencil.png")

        @JvmStatic
        fun drawIcon(g: GuiGraphics, w: Int, h: Int) {
            g.blit(PENCIL, (w - 9) / 2, (h - 9) / 2, 9, 9, 0f, 0f, 13, 13, 13, 13)
        }
    }
}
