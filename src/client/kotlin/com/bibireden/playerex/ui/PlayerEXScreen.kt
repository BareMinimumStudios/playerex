package com.bibireden.playerex.ui

import com.bibireden.data_attributes.ui.renderers.ButtonRenderers
import com.bibireden.playerex.PlayerEXClient
import com.bibireden.playerex.components.player.IPlayerDataComponent
import com.bibireden.playerex.ext.component
import com.bibireden.playerex.ext.level
import com.bibireden.playerex.networking.NetworkingChannels
import com.bibireden.playerex.networking.NetworkingPackets
import com.bibireden.playerex.networking.types.UpdatePacketType
import com.bibireden.playerex.registry.PlayerEXMenuRegistry
import com.bibireden.playerex.ui.components.MenuComponent
import com.bibireden.playerex.ui.components.MenuComponent.OnLevelUpdated
import com.bibireden.playerex.ui.components.buttons.AttributeButtonComponent
import com.bibireden.playerex.ui.helper.InputHelper
import com.bibireden.playerex.ui.util.Colors
import com.bibireden.playerex.util.PlayerEXUtil
import io.wispforest.endec.impl.StructEndecBuilder
import io.wispforest.owo.ui.base.BaseUIModelScreen
import io.wispforest.owo.ui.component.*
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.core.Component as OwoComponent
import io.wispforest.owo.util.EventSource
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.attributes.Attribute
import kotlin.reflect.KClass

// Transformers
fun <T : OwoComponent> ParentComponent.childById(clazz: KClass<T>, id: String) = this.childById(clazz.java, id)

/** Primary screen for the mod that brings everything intended together. */
class PlayerEXScreen : BaseUIModelScreen<FlowLayout>(FlowLayout::class.java, DataSource.asset(PlayerEXClient.MAIN_UI_SCREEN_ID)) {
    private var listCollapsed = false

    private var currentPage = 0

    private val pages: MutableList<Pair<ResourceLocation, MenuComponent>> = mutableListOf()

    private val player by lazy { this.minecraft!!.player!! }

    private val content by lazy { uiAdapter.rootComponent.childById(FlowLayout::class, "content")!! }
    private val footer by lazy { uiAdapter.rootComponent.childById(FlowLayout::class, "footer")!! }

    private val currentLevel by lazy { uiAdapter.rootComponent.childById(LabelComponent::class, "level:current")!! }
    private val levelAmount by lazy { uiAdapter.rootComponent.childById(TextBoxComponent::class, "level:amount")!! }
    private val levelButton by lazy { uiAdapter.rootComponent.childById(ButtonComponent::class, "level:button")!! }


    private val onLevelUpdatedEvents = OnLevelUpdated.stream
    private val onLevelUpdated: EventSource<OnLevelUpdated> = onLevelUpdatedEvents.source()

    override fun isPauseScreen(): Boolean = false

    /** Whenever the level attribute gets modified, and on initialization of the screen, this will be called. */
    fun onLevelUpdated(level: Int) {
        currentLevel.apply {
            text(Component.translatable("playerex.ui.current_level", player.level.toInt(), PlayerEXUtil.getRequiredXpForNextLevel(player)))
        }

        updatePointsAvailable()
        updateLevelUpButton()
        updateProgressBar()

        this.uiAdapter.rootComponent.forEachDescendant { descendant ->
            if (descendant is MenuComponent) descendant.onLevelUpdatedEvents.sink().onLevelUpdated(level)
            if (descendant is AttributeButtonComponent) descendant.refresh()
        }
    }

    /** Whenever any attribute is updated, this will be called. */
    fun onAttributeUpdated(attribute: Attribute, value: Double) {
        this.uiAdapter.rootComponent.forEachDescendant { descendant ->
            if (descendant is MenuComponent) descendant.onAttributeUpdatedEvents.sink().onAttributeUpdated(attribute, value)
            if (descendant is AttributeButtonComponent) descendant.refresh()
        }
        updatePointsAvailable()
    }

    private fun updatePointsAvailable() {
        this.uiAdapter.rootComponent.childById(LabelComponent::class, "points_available")?.apply {
            text(Component.translatable("playerex.ui.main.skill_points_header").append(": ").append(
                Component.literal("${player.component.skillPoints}").withStyle {
                    it.withColor(
                        when (player.component.skillPoints) {
                            0 -> Colors.GRAY
                            else -> Colors.SATURATED_BLUE
                        }
                    )
                })
            )
        }
    }

    /** changes the page based on the index. */
    private fun switchPage(to: Int) {
        val root = this.uiAdapter.rootComponent

        val content = root.childById(FlowLayout::class, "content")!!

        content.clearChildren()
        content.child(pages[to].second)

        this.onLevelUpdated(player.level.toInt())
    }

    private fun updateLevelUpButton() {
        val amount = levelAmount.value.toIntOrNull() ?: return
        val result = player.level + amount

        levelButton
            .active(player.experienceLevel >= PlayerEXUtil.getRequiredXpForLevel(player, result))
            .tooltip(Component.translatable("playerex.ui.level_button", PlayerEXUtil.getRequiredXpForLevel(player, result), amount, player.experienceLevel))
    }

    private fun updateProgressBar() {
        var result = 0.0
        if (player.experienceLevel > 0) {
            val required = PlayerEXUtil.getRequiredXpForNextLevel(player)
            result = Mth.clamp((player.experienceLevel.toDouble() / required) * 100, 0.0, 100.0)
        }
       footer.childById(BoxComponent::class, "progress")?.horizontalSizing()?.animate(250, Easing.CUBIC, Sizing.fill(result.toInt()))?.forwards()
    }

    private fun updateCollapseState(content: FlowLayout, listLayout: FlowLayout, button: ButtonComponent) {
        button.message = Component.literal(if (listCollapsed) "<" else ">").withStyle(Style.EMPTY.withBold(true))

        content.horizontalSizing(if (listCollapsed) Sizing.fill(97) else Sizing.fill(90))
        button.horizontalSizing(if (listCollapsed) Sizing.fixed(10) else Sizing.fill(10))
        listLayout.horizontalSizing(Sizing.fill(if (listCollapsed) 3 else 12))
    }

    override fun build(rootComponent: FlowLayout) {
        val listLayout = rootComponent.childById(FlowLayout::class, "list")!!
        val contentLayout = rootComponent.childById(FlowLayout::class, "content")!!

        PlayerEXMenuRegistry.get().forEach { (resource, clazz) ->
            val instance = clazz.getDeclaredConstructor().newInstance()
            instance.init(minecraft!!, this, player.component)
            instance.build(contentLayout)
            pages.add(Pair(resource, instance))
        }

        listLayout.child(
            Components.button(Component.empty()) {
                listCollapsed = !listCollapsed
                updateCollapseState(contentLayout, listLayout, it)
            }
                .apply {
                    renderer(ButtonRenderers.STANDARD)
                    textShadow(false)
                    verticalSizing(Sizing.fill(100))

                    updateCollapseState(contentLayout, listLayout, this)
                }
        )

        listLayout.child(
            Containers.verticalScroll(Sizing.fill(90), Sizing.fill(100), Containers.verticalFlow(Sizing.content(), Sizing.content())
                .apply {
                    gap(2)
                    padding(Insets.of(2))
                    verticalAlignment(VerticalAlignment.TOP)
                    horizontalAlignment(HorizontalAlignment.LEFT)
                }
                .also { vf ->
                    pages.forEachIndexed { index, (resource) ->
                        vf.child(
                            Components.button(Component.translatable("playerex.ui.menu.${resource.toLanguageKey()}")) { this.switchPage(index) }
                                .renderer(ButtonRenderers.STANDARD)
                        )
                    }
                }
            )
                .apply {
                    positioning(Positioning.relative(100, 0))
                }
                .id("scroll")
        )

        val levelUpButton = rootComponent.childById(ButtonComponent::class, "level:button")!!
        val exit = rootComponent.childById(ButtonComponent::class, "exit")!!


        levelAmount.setFilter(InputHelper::isUIntInput)
        levelAmount.onChanged().subscribe { updateLevelUpButton() }

        onLevelUpdated(player.level.toInt())
        onExperienceUpdated()

        switchPage(0)

        onLevelUpdated.subscribe { updateLevelUpButton() }

        levelUpButton.onPress { levelAmount.value.toIntOrNull()?.let { NetworkingChannels.MODIFY.clientHandle().send(NetworkingPackets.Level(it)) } }
        exit.onPress { this.onClose() }
    }

    /** Whenever the player's experience is changed, refreshing the current status of experience-tied ui elements. */
    fun onExperienceUpdated() {
        this.uiAdapter.rootComponent.childById(LabelComponent::class.java, "experience")!!.apply {
            text(Component.literal(player.experienceLevel.toString()))
        }
        updateLevelUpButton()
        updateProgressBar()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (PlayerEXClient.KEYBINDING_MAIN_SCREEN.matches(keyCode, scanCode)) {
            this.onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    enum class ButtonType {
        Add,
        Remove;

        fun getPointsFromComponent(component: IPlayerDataComponent): Int = if (this == Add) component.skillPoints else component.refundablePoints

        val symbol: String
            get() = if (this == Add) "+" else "-"

        val packet: UpdatePacketType
            get() = when (this) {
                Add -> UpdatePacketType.Skill
                Remove -> UpdatePacketType.Refund
            }
    }
}