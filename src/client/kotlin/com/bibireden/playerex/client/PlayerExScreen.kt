package com.bibireden.playerex.client

import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.config.PlayerExConfigState
import com.bibireden.playerex.networking.PlayerExClientRequests
import com.bibireden.playerex.math.ProgressionMath
import com.bibireden.playerex.state.ClientPlayerExState
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import kotlin.math.min

class PlayerExScreen : Screen(tr("title")) {
    private enum class Page(val key: String, val icon: String) {
        COMBAT("combat", "icon_swords"), ATTRIBUTES("attributes", "icon_helix"), SPELLS("spells", "icon_bandage")
    }
    private enum class Control { TEXT, CLOSE, LEVEL, INVENTORY }
    private var displayedCost = 0
    private var costLevel = -1
    private var costAmount = -1
    private var costFormula: String? = null
    private var valuesTick = -1
    private data class StatText(val name: Component, val value: Component, val width: Int, val available: Boolean, val icon: Component?)
    private val shownValues = mutableMapOf<ResourceLocation, StatText>()
    private val primary = PlayerEXAttributes.PRIMARY_ATTRIBUTE_IDS.toList()
    private var allocationTargets = primary
    private var mana: ManaPresentation.Value? = null
    private val panelHeight: Int get() = if (page == Page.SPELLS) (if (ManaPresentation.available) 352 else 328) else 288
    private fun availableSchools() = minecraft?.player?.let { player ->
        PlayerEXAttributes.SPELL_SCHOOL_IDS.keys.filter { PlayerEXAttributes.schoolAvailable(player, it) }
    } ?: emptyList()
    private val icons = listOf("icon_heart", "icon_arm", "icon_bow", "icon_helix", "icon_clover", "icon_eye")
    private val spend = mutableListOf<Button>()
    private val refunds = mutableListOf<Button>()
    private lateinit var levelButton: Button
    private var page = Page.ATTRIBUTES
    private var originX = 0
    private var originY = 0
    private var scale = 1f
    private var ticks = 0
    private var nextRequestTick = 0
    private var allocationAmount = 1
    private var levelAmount = 1
    private val ids = mutableMapOf<String, ResourceLocation>()
    private var hoveredAttribute: ResourceLocation? = null
    private var tooltipId: ResourceLocation? = null
    private var tooltipTick = -1
    private var attributeTooltip = emptyList<FormattedCharSequence>()

    override fun init() {
        scale = min(1f, min((width - 12) / (PANEL_WIDTH + 20f), (height - 12) / (panelHeight + 20f))).coerceAtLeast(0.25f)
        originX = ((width - PANEL_WIDTH * scale) / 2).toInt()
        originY = ((height - (panelHeight - 20) * scale) / 2).toInt()
        spend.clear()
        refunds.clear()
        tooltipId = null
        shownValues.clear()
        Page.entries.forEachIndexed { i, tab ->
            button(18 + i * 102, -18, 100, 19, tr(tab.key), tab.icon, "top_tab") {
                page = tab; tooltipId = null; rebuildWidgets()
            }.tooltip = Tooltip.create(tr(tab.key))
        }
        button(PANEL_WIDTH - 20, 5, 14, 16, tr("close"), control = Control.CLOSE) { onClose() }
            .tooltip = Tooltip.create(tr("close_hint"))
        button(PANEL_WIDTH + 1, 63, 12, 21, tr("inventory"), control = Control.INVENTORY) {
            minecraft?.player?.let { minecraft?.setScreen(InventoryScreen(it)) }
        }.tooltip = Tooltip.create(tr("inventory"))
        allocationTargets = if (page == Page.SPELLS) availableSchools() else primary
        if (page != Page.COMBAT) {
            button(139, 49, 25, 13, Component.literal("×$allocationAmount")) {
                allocationAmount = nextAmount(allocationAmount); rebuildWidgets()
            }.tooltip = Tooltip.create(tr("amount_hint"))
            allocationTargets.forEachIndexed { i, id ->
            val y = 80 + i * if (page == Page.SPELLS) 16 else 22
            refunds += button(139, y - 2, 12, 13, Component.literal("−")) {
                request { PlayerExClientRequests.refund(id, allocationAmount) }
            }.also { it.tooltip = Tooltip.create(tr("refund_hint")) }
            spend += button(152, y - 2, 12, 13, Component.literal("+")) {
                request { PlayerExClientRequests.skill(id, allocationAmount) }
            }.also { it.tooltip = Tooltip.create(tr("spend_hint")) }
            }
        }
        button(272, panelHeight - 40, 25, 11, Component.literal("×$levelAmount")) {
            levelAmount = nextAmount(levelAmount); rebuildWidgets()
        }.tooltip = Tooltip.create(tr("level_amount_hint"))
        levelButton = button(302, panelHeight - 27, 7, 13, tr("level_hint"), control = Control.LEVEL) {
            request { PlayerExClientRequests.level(levelAmount) }
        }.also { it.tooltip = Tooltip.create(tr("level_hint")) }
        updateButtons()
    }

    /** Keep native input/focus/narration; draw asset-backed controls instead of vanilla rectangles. */
    private inner class ArtButton(private val lx: Int, private val ly: Int, private val lw: Int, private val lh: Int,
        label: Component, private val icon: String?, private val background: String?, private val control: Control, action: () -> Unit) :
        Button(originX + (lx * scale).toInt(), originY + (ly * scale).toInt(),
            (lw * scale).toInt().coerceAtLeast(1), (lh * scale).toInt().coerceAtLeast(1),
            label, OnPress { action() }, DEFAULT_NARRATION) {
        override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            g.pose().pushPose(); g.pose().translate(originX + lx * scale, originY + ly * scale, 0f); g.pose().scale(scale, scale, 1f)
            if (background != null) {
                if (background == "top_tab") {
                    assetSlice(g, background, 0, 0, 6, lh, 0, 0, 6, 19, 36, 19)
                    assetSlice(g, background, 6, 0, lw - 12, lh, 16, 0, 1, 19, 36, 19)
                    assetSlice(g, background, lw - 6, 0, 6, lh, 30, 0, 6, 19, 36, 19)
                } else texture(g, background, 0, 0, lw, lh, 19, 55)
            }
            val selected = background == "top_tab" && icon == page.icon
            val color = if (control == Control.CLOSE) { if (isHoveredOrFocused) 0xFF7169 else 0xDC4942 }
                else if (control == Control.LEVEL) { if (!active) 0xBDA15E else if (isHoveredOrFocused) 0xFFE577 else 0xF2D36B }
                else if (!active) MUTED else if (isHoveredOrFocused || selected) GOLD else WHITE
            if (control == Control.TEXT && background == null && icon == null && lw == 25) {
                val border = 0x807B806E.toInt()
                g.fill(0, 0, lw, 1, border); g.fill(0, lh - 1, lw, lh, border)
                g.fill(0, 0, 1, lh, border); g.fill(lw - 1, 0, lw, lh, border)
            }
            if (control == Control.LEVEL && active && isHoveredOrFocused) {
                // Follow the curved end cap instead of highlighting its rectangular hitbox.
                g.fill(1, 2, lw - 1, lh - 2, 0x506F582B)
            } else if (control != Control.INVENTORY && ((active && isHoveredOrFocused) || selected)) {
                val inset = if (background == "top_tab") 4 else 1
                val top = if (background == "top_tab") 4 else 1
                g.fill(inset, top, lw - inset, lh - 2, 0x305F512D)
                if (!selected || isHoveredOrFocused) g.fill(inset, top, lw - inset, top + 1, opaque(GOLD))
                if (!selected || isHoveredOrFocused) g.fill(inset, top, inset + 1, lh - 2, opaque(GOLD))
                if (!selected || isHoveredOrFocused) g.fill(lw - inset - 1, top, lw - inset, lh - 2, opaque(GOLD))
                g.fill(inset, lh - 3, lw - inset, lh - 2, (0xFF000000L or GOLD.toLong()).toInt())
            }
            when (control) {
                Control.CLOSE -> {
                    for (i in -3..3) {
                        g.fill(lw / 2 + i, lh / 2 + i, lw / 2 + i + 1, lh / 2 + i + 1, opaque(color))
                        g.fill(lw / 2 + i, lh / 2 - i, lw / 2 + i + 1, lh / 2 - i + 1, opaque(color))
                    }
                }
                Control.LEVEL -> {
                    g.fill(lw / 2 - 2, lh / 2, lw / 2 + 3, lh / 2 + 1, opaque(color))
                    g.fill(lw / 2, lh / 2 - 2, lw / 2 + 1, lh / 2 + 3, opaque(color))
                }
                Control.INVENTORY -> InventoryLinkButton.drawIcon(g, lw, lh)
                Control.TEXT -> if (icon != null) texture(g, icon, (lw - 13) / 2, (lh - 13) / 2, 13, 13, 13, 13)
                    else centered(g, message, lw / 2, (lh - 7) / 2, color)
            }
            g.pose().popPose()
        }
    }
    private fun button(x: Int, y: Int, w: Int, h: Int, label: Component, icon: String? = null,
        background: String? = null, control: Control = Control.TEXT, action: () -> Unit): Button = addRenderableWidget(ArtButton(x, y, w, h, label, icon, background, control, action))
    private fun request(action: () -> Unit) {
        if (ticks < nextRequestTick) return
        action(); nextRequestTick = ticks + 5; updateButtons()
    }
    override fun tick() {
        ticks++
        if (PlayerExConfigState.current().disableUI) { onClose(); return }
        if (minecraft?.player == null) onClose()
        else if (page == Page.SPELLS && allocationTargets != availableSchools()) rebuildWidgets()
        else updateButtons()
        mana = minecraft?.player?.let(ManaPresentation::value)
    }
    private fun updateButtons() {
        val state = ClientPlayerExState.current()
        val ready = minecraft?.player != null && ticks >= nextRequestTick
        displayedCost = cost()
        allocationTargets.forEachIndexed { i, id ->
            if (i < spend.size) {
                val count = state.allocations[id] ?: 0
                spend[i].active = ready && state.skillPoints >= allocationAmount && count.toLong() + allocationAmount <=
                    (AttributePresentation.maximum(id) ?: PlayerEXAttributes.get(id)?.maxValue ?: 0.0)
                refunds[i].active = ready && state.refundablePoints >= allocationAmount && count >= allocationAmount
            }
        }
        levelButton.active = ready && state.level.toLong() + levelAmount <= (AttributePresentation.maximum(PlayerEXAttributes.LEVEL_ID) ?: 100.0) &&
            (minecraft?.player?.experienceLevel ?: 0) >= displayedCost
    }
    private fun cost(): Int {
        val level = ClientPlayerExState.current().level
        val formula = PlayerExConfigState.current().levelFormula
        if (costLevel == level && costAmount == levelAmount && costFormula == formula) return displayedCost
        costLevel = level; costAmount = levelAmount; costFormula = formula
        return ProgressionMath.costBetweenLevels(level, level + levelAmount) { PlayerExConfigState.formulaEngine().levelCost(it.toDouble()) }
    }
    private fun nextAmount(current: Int): Int = AMOUNTS[(AMOUNTS.indexOf(current) + 1) % AMOUNTS.size]

    // Screen.render normally draws the background before widgets. Do that pass BEFORE our artwork.
    override fun renderBackground(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) { }
    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderBackground(g, mouseX, mouseY, partialTick)
        hoveredAttribute = null
        if (valuesTick != ticks) { shownValues.clear(); valuesTick = ticks }
        g.pose().pushPose(); g.pose().translate(originX.toFloat(), originY.toFloat(), 0f); g.pose().scale(scale, scale, 1f)
        frame(g)
        texture(g, "ivy", PANEL_WIDTH - 45, -12, 61, 89, 61, 89)
        texture(g, "right_tab", PANEL_WIDTH - 1, 45, 19, 55, 19, 55)
        displayText(g, Component.literal(tr(page.key).string.uppercase(java.util.Locale.ROOT)), 80f, 9f, 1.2f, 132f, bold = false)
        displayText(g, tr("level", ClientPlayerExState.current().level), 239f, 9f, 1.2f, 94f, outline = true)
        when (page) {
            Page.ATTRIBUTES -> attributes(g, mouseX, mouseY)
            Page.COMBAT -> combat(g, mouseX, mouseY)
            Page.SPELLS -> spells(g, mouseX, mouseY)
        }
        footer(g); g.pose().popPose()
        super.render(g, mouseX, mouseY, partialTick)
        hoveredAttribute?.let { id -> minecraft?.player?.let { player ->
            if (tooltipId != id || tooltipTick != ticks) {
                tooltipId = id; tooltipTick = ticks; attributeTooltip = AttributePresentation.tooltip(player, id).flatMap { font.split(it, min(220, width - 24).coerceAtLeast(80)) }
            }
            setTooltipForNextRenderPass(attributeTooltip)
        } }
    }
    /** Expand blank header sections and rails while keeping the crest and corner details intact. */
    private fun frame(g: GuiGraphics) {
        g.fill(5, 25, PANEL_WIDTH - 5, panelHeight - 5, 0xF0181C1B.toInt())
        // The crest's diagonal stonework extends beyond the shield itself.
        // Keep source columns 56..119 together; stretch only the plain header rails.
        val capWidth = 56
        val crestWidth = 64
        val crestLeft = (PANEL_WIDTH - crestWidth) / 2
        val railWidth = crestLeft - capWidth
        slice(g, 0, 0, capWidth, 50, 0, 0, capWidth, 50)
        slice(g, capWidth, 0, railWidth, 28, 48, 0, 1, 28)
        slice(g, crestLeft, 0, crestWidth, 50, capWidth, 0, crestWidth, 50)
        slice(g, crestLeft + crestWidth, 0, railWidth, 28, 128, 0, 1, 28)
        slice(g, PANEL_WIDTH - capWidth, 0, capWidth, 50, 120, 0, capWidth, 50)
        slice(g, 0, 50, 6, panelHeight - 58, 0, 50, 6, 146)
        slice(g, PANEL_WIDTH - 6, 50, 6, panelHeight - 58, 170, 50, 6, 146)
        slice(g, 0, panelHeight - 8, 6, 8, 0, 196, 6, 8)
        slice(g, 6, panelHeight - 8, PANEL_WIDTH - 12, 8, 80, 196, 1, 8)
        slice(g, PANEL_WIDTH - 6, panelHeight - 8, 6, 8, 170, 196, 6, 8)
    }
    private fun attributes(g: GuiGraphics, mx: Int, my: Int) {
        val state = ClientPlayerExState.current()
        text(g, tr("skill_points", state.skillPoints), 12, 48, WHITE)
        text(g, tr("refund_points", state.refundablePoints), 12, 59, MUTED)
        primary.forEachIndexed { i, id ->
            val y = 80 + i * 22
            texture(g, icons[i], 12, y - 2, 13, 13, 13, 13)
            text(g, AttributePresentation.name(id), 28, y, WHITE)
            val cap = (AttributePresentation.maximum(id) ?: PlayerEXAttributes.get(id)?.maxValue ?: 100.0).toInt()
            val count = Component.literal("${state.allocations[id] ?: 0}/$cap")
            text(g, count, 137 - textWidth(count), y, GOLD)
            // Light row guides leave the supplied ornate dividers for section headings.
            g.fill(28, y + 16, 163, y + 17, 0x405F685D)
            hover(id, 12, y - 2, 125, 15, mx, my)
        }
        heading(g, "vitality", "icon_heart", 180, 48)
        stat(g, "playerex:health_regeneration", 180, 69, mx, my)
        stat(g, "playerex:heal_amplification", 180, 86, mx, my)
        stat(g, "minecraft:generic.movement_speed", 180, 103, mx, my)
        heading(g, "resistances", "icon_bandage", 180, 126)
        RESISTANCE_TYPES.forEachIndexed { i, id -> stat(g, "playerex:${id}_resistance", 180, 147 + i * 15, mx, my) }
        minecraft?.player?.let { player ->
            centered(g, tr("health", AttributePresentation.number(player.health.toDouble()), AttributePresentation.number(player.maxHealth.toDouble())), 89, panelHeight - 68, WHITE)
            bar(g, 24, panelHeight - 57, 130, "progress_bar_red", player.health / player.maxHealth.toDouble())
            centered(g, tr("oxygen", player.airSupply.coerceAtLeast(0), player.maxAirSupply), 249, panelHeight - 68, WHITE)
            bar(g, 184, panelHeight - 57, 130, "progress_bar_blue", player.airSupply / player.maxAirSupply.toDouble())
        }
    }
    private fun combat(g: GuiGraphics, mx: Int, my: Int) {
        heading(g, "melee", "icon_swords", 12, 48)
        MELEE_STATS
            .forEachIndexed { i, id -> stat(g, id, 12, 69 + i * 16, mx, my) }
        heading(g, "defense", "icon_shield", 12, 145)
        DEFENSE_STATS
            .forEachIndexed { i, id -> stat(g, id, 12, 166 + i * 15, mx, my) }
        heading(g, "ranged", "icon_bow", 180, 48)
        RANGED_STATS
            .forEachIndexed { i, id -> stat(g, id, 180, 69 + i * 16, mx, my) }
        heading(g, "miscellaneous", "icon_running", 180, 145)
        MISCELLANEOUS_STATS
            .forEachIndexed { i, id -> stat(g, id, 180, 166 + i * 15, mx, my) }
    }
    private fun spells(g: GuiGraphics, mx: Int, my: Int) {
        val state = ClientPlayerExState.current()
        text(g, tr("skill_points", state.skillPoints), 12, 48, WHITE)
        text(g, tr("refund_points", state.refundablePoints), 12, 59, MUTED)
        allocationTargets.forEachIndexed { i, allocation ->
            val y = 80 + i * 16
            SpellSchoolSymbols.icon(allocation)?.let { text(g, it, 12, y, WHITE) }
            text(g, AttributePresentation.name(allocation), 23, y, WHITE)
            val count = Component.literal((state.allocations[allocation] ?: 0).toString())
            text(g, count, 137 - textWidth(count), y, GOLD)
            hover(allocation, 12, y - 2, 125, 15, mx, my)
        }
        if (allocationTargets.isEmpty()) {
            text(g, tr("spells_unavailable"), 12, 80, MUTED)
            font.split(tr("spells_optional"), (146 / BODY_SCALE).toInt()).forEachIndexed { i, line -> text(g, line, 12, 94 + i * 10, MUTED) }
        }
        heading(g, "spell_power", "icon_helix", 180, 48)
        allocationTargets.forEachIndexed { i, allocation ->
            stat(g, PlayerEXAttributes.SPELL_SCHOOL_IDS.getValue(allocation).toString(), 180, 69 + i * 14, mx, my)
        }
        heading(g, "spell_criticals", "icon_clover", 180, 215)
        SPELL_CRITICAL_STATS.forEachIndexed { i, id -> stat(g, id, 180, 236 + i * 14, mx, my) }
        mana?.let { value ->
            centered(g, tr("mana", String.format(java.util.Locale.ROOT, "%.1f", value.current), String.format(java.util.Locale.ROOT, "%.1f", value.maximum)), 168, panelHeight - 67, WHITE)
            bar(g, 36, panelHeight - 55, 264, "progress_bar_blue", if (value.maximum > 0) (value.current / value.maximum).toDouble() else 0.0)
        }
    }
    private fun heading(g: GuiGraphics, key: String, icon: String, x: Int, y: Int) {
        texture(g, icon, x, y - 3, 13, 13, 13, 13); headerText(g, tr(key), x + 16, y, GOLD)
        divider(g, x, y + 10, 144)
    }
    private fun divider(g: GuiGraphics, x: Int, y: Int, w: Int) {
        assetSlice(g, "small_divider", x, y, 4, 6, 0, 0, 4, 6, 79, 6)
        assetSlice(g, "small_divider", x + 4, y, w - 8, 6, 40, 0, 1, 6, 79, 6)
        assetSlice(g, "small_divider", x + w - 4, y, 4, 6, 75, 0, 4, 6, 79, 6)
    }
    private fun stat(g: GuiGraphics, raw: String, x: Int, y: Int, mx: Int, my: Int) {
        val id = id(raw); val player = minecraft?.player ?: return
        val shown = shownValues.getOrPut(id) {
            val current = AttributePresentation.value(player, id)
            val name = if (BuiltInRegistries.ATTRIBUTE.containsKey(id)) AttributePresentation.displayName(id)
                else tr("optional_${id.path.substringAfterLast('.')}")
            val value = Component.literal(AttributePresentation.displayed(player, id, current))
            val width = textWidth(value)
            val icon = SpellSchoolSymbols.icon(id)
            val inset = if (icon == null) 0 else 11
            val labelWidth = ((138 - inset - width) / BODY_SCALE).toInt().coerceAtLeast(0)
            StatText(Component.literal(font.plainSubstrByWidth(name.string, labelWidth)), value, width, current != null, icon)
        }
        val inset = if (shown.icon == null) 0 else 11
        shown.icon?.let { text(g, it, x, y, WHITE) }
        text(g, shown.name, x + inset, y, if (shown.available) WHITE else MUTED)
        text(g, shown.value, x + 146 - shown.width, y, if (shown.available) GOLD else MUTED)
        if (shown.available) hover(id, x, y - 1, 146, 11, mx, my)
    }
    private fun footer(g: GuiGraphics) {
        val state = ClientPlayerExState.current(); val xp = minecraft?.player?.experienceLevel ?: 0; val needed = displayedCost
        val max = state.level >= (AttributePresentation.maximum(PlayerEXAttributes.LEVEL_ID) ?: 100.0)
        assetSlice(g, "bottom_hud_disconnected", 9, panelHeight - 45, 44, 34, 0, 0, 44, 34, 159, 34)
        assetSlice(g, "bottom_hud_disconnected", 53, panelHeight - 45, PANEL_WIDTH - 93, 34, 90, 0, 1, 34, 159, 34)
        assetSlice(g, "bottom_hud_disconnected", PANEL_WIDTH - 40, panelHeight - 45, 16, 34, 143, 0, 16, 34, 159, 34)
        // Visible inner face: source x=11..38, y=10..32, translated by (9, panelHeight-45).
        displayText(g, Component.literal(state.level.toString()), 34f, panelHeight - 28f, 1.25f, 24f)
        // The supplied 92×3 gold strip belongs in the HUD's recessed lane (source rows 24–26).
        val fill = (243 * (if (max || needed <= 0) 1.0 else (xp.toDouble() / needed).coerceIn(0.0, 1.0))).toInt()
        if (fill > 0) {
            g.enableScissor(originX + (55 * scale).toInt(), originY + ((panelHeight - 21) * scale).toInt(),
                originX + ((55 + fill) * scale).toInt(), originY + ((panelHeight - 18) * scale).toInt())
            texture(g, "progress_bar_bottom", 55, panelHeight - 21, 243, 3, 92, 3)
            g.disableScissor()
        }
        centered(g, if (max) tr("max_level") else tr("xp", xp, needed), PANEL_WIDTH / 2, panelHeight - 40, WHITE)
    }
    private fun bar(g: GuiGraphics, x: Int, y: Int, w: Int, name: String, fraction: Double) {
        texture(g, "progress_bar_empty", x, y, w, 8, 122, 8)
        val end = (w * fraction.coerceIn(0.0, 1.0)).toInt()
        if (end > 0) {
            g.enableScissor(originX + (x * scale).toInt(), originY + (y * scale).toInt(), originX + ((x + end) * scale).toInt(), originY + ((y + 8) * scale).toInt())
            texture(g, name, x, y, w, 8, 122, 8); g.disableScissor()
        }
    }
    private fun hover(id: ResourceLocation, x: Int, y: Int, w: Int, h: Int, mx: Int, my: Int) {
        val lx = (mx - originX) / scale; val ly = (my - originY) / scale
        if (lx >= x && lx < x + w && ly >= y && ly < y + h) hoveredAttribute = id
    }
    private fun opaque(color: Int): Int = color or 0xFF000000.toInt()
    private fun displayText(g: GuiGraphics, c: Component, centerX: Float, y: Float, requestedScale: Float, availableWidth: Float, color: Int = 0xF2D36B, bold: Boolean = true, outline: Boolean = false) {
        val label = if (bold) c.copy().withStyle(ChatFormatting.BOLD) else c
        val textWidth = font.width(label)
        val size = min(requestedScale, availableWidth / textWidth.coerceAtLeast(1))
        g.pose().pushPose(); g.pose().translate(centerX - textWidth * size / 2f, y, 0f); g.pose().scale(size, size, 1f)
        if (outline) {
            g.drawString(font, label, -1, 0, LEVEL_OUTLINE, false)
            g.drawString(font, label, 1, 0, LEVEL_OUTLINE, false)
            g.drawString(font, label, 0, -1, LEVEL_OUTLINE, false)
            g.drawString(font, label, 0, 1, LEVEL_OUTLINE, false)
        }
        g.drawString(font, label, 0, 0, color, false)
        g.pose().popPose()
    }
    private fun headerText(g: GuiGraphics, c: Component, x: Int, y: Int, color: Int) { g.drawString(font, c, x, y, color, false) }
    private fun textWidth(c: Component): Int = kotlin.math.ceil(font.width(c) * BODY_SCALE).toInt()
    private fun text(g: GuiGraphics, c: Component, x: Int, y: Int, color: Int) {
        text(g, c.visualOrderText, x, y, color)
    }
    private fun text(g: GuiGraphics, c: FormattedCharSequence, x: Int, y: Int, color: Int) {
        g.pose().pushPose(); g.pose().translate(x.toFloat(), y.toFloat(), 0f); g.pose().scale(BODY_SCALE, BODY_SCALE, 1f)
        g.drawString(font, c, 0, 0, color, false); g.pose().popPose()
    }
    private fun centered(g: GuiGraphics, c: Component, x: Int, y: Int, color: Int) { text(g, c, x - textWidth(c) / 2, y, color) }
    private fun texture(g: GuiGraphics, name: String, x: Int, y: Int, w: Int, h: Int, tw: Int, th: Int) {
        g.blit(id("playerex:textures/gui/poke/$name.png"), x, y, w, h, 0f, 0f, tw, th, tw, th)
    }
    private fun slice(g: GuiGraphics, x: Int, y: Int, w: Int, h: Int, u: Int, v: Int, sw: Int, sh: Int) {
        assetSlice(g, "base_nobottom", x, y, w, h, u, v, sw, sh, 176, 204)
    }
    private fun assetSlice(g: GuiGraphics, name: String, x: Int, y: Int, w: Int, h: Int, u: Int, v: Int, sw: Int, sh: Int, tw: Int, th: Int) {
        g.blit(id("playerex:textures/gui/poke/$name.png"), x, y, w, h, u.toFloat(), v.toFloat(), sw, sh, tw, th)
    }
    private fun id(raw: String): ResourceLocation = ids.getOrPut(raw) { ResourceLocation.parse(raw) }
    override fun isPauseScreen() = false
    companion object {
        private const val PANEL_WIDTH = 340
        private const val BODY_SCALE = 0.85f
        private const val LEVEL_OUTLINE = 0x58431E
        private const val GOLD = 0xE1C98A
        private const val WHITE = 0xE9E5D9
        private const val MUTED = 0x90978C
        private val RESISTANCE_TYPES = listOf("fire", "freeze", "lightning", "poison", "wither")
        private val MELEE_STATS = listOf("minecraft:generic.attack_damage", "minecraft:generic.attack_speed", "playerex:melee_crit_damage", "playerex:melee_crit_chance")
        private val DEFENSE_STATS = listOf("minecraft:generic.armor", "minecraft:generic.armor_toughness", "minecraft:generic.knockback_resistance", "playerex:evasion", "spell_power:resistance.generic")
        private val RANGED_STATS = listOf("ranged_weapon:damage", "ranged_weapon:haste", "playerex:ranged_crit_damage", "playerex:ranged_crit_chance")
        private val MISCELLANEOUS_STATS = listOf("minecraft:player.entity_interaction_range", "minecraft:player.block_interaction_range", "playerex:breaking_speed", "playerex:lifesteal", "playerex:dropped_experience_multiplier")
        private val SPELL_CRITICAL_STATS = listOf("spell_power:critical_chance", "spell_power:critical_damage", "spell_power:haste")
        private val AMOUNTS = listOf(1, 5, 10, 25)
        private fun tr(key: String, vararg args: Any): Component = Component.translatable("playerex.screen.$key", *args)
        fun open() { val c = Minecraft.getInstance(); if (c.player != null && !PlayerExConfigState.current().disableUI) c.setScreen(PlayerExScreen()) }
    }
}
