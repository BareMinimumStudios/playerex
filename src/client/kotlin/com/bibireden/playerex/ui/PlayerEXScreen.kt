package com.bibireden.playerex.ui

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
import io.wispforest.owo.ui.base.BaseUIModelScreen
import io.wispforest.owo.ui.component.*
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Component as OwoComponent
import io.wispforest.owo.ui.core.Easing
import io.wispforest.owo.ui.core.ParentComponent
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.util.EventSource
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.network.chat.Component
import kotlin.reflect.KClass

// Transformers
fun <T : OwoComponent> ParentComponent.childById(clazz: KClass<T>, id: String) = this.childById(clazz.java, id)

/** Primary screen for the mod that brings everything intended together. */
class PlayerEXScreen : BaseUIModelScreen<FlowLayout>(FlowLayout::class.java, DataSource.asset(PlayerEXClient.MAIN_UI_SCREEN_ID)) {
    private var currentPage = 0

    private val pages: MutableList<MenuComponent> = mutableListOf()

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
        val root = this.uiAdapter.rootComponent

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
            text(Component.translatable("playerex.ui.main.skill_points_header").append(": [").append(
                Component.literal("${player.component.skillPoints}").withStyle {
                    it.withColor(
                        when (player.component.skillPoints) {
                            0 -> Colors.GRAY
                            else -> Colors.SATURATED_BLUE
                        }
                    )
                }).append("]")
            )
        }
    }

    private fun onPagesUpdated() {
        val root = this.uiAdapter.rootComponent
        val pageCounter = root.childById(LabelComponent::class, "counter")!!
        val content = root.childById(FlowLayout::class, "content")!!

        pageCounter.text(Component.nullToEmpty("${currentPage + 1}/${pages.size}"))
        content.clearChildren()
        content.child(pages[currentPage])
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
       footer.childById(BoxComponent::class, "progress")!!
            .horizontalSizing().animate(1000, Easing.CUBIC, Sizing.fill(result.toInt())).forwards()
    }

    override fun build(rootComponent: FlowLayout) {
        val player = minecraft?.player ?: return

        val levelUpButton = rootComponent.childById(ButtonComponent::class, "level:button")!!

        updateLevelUpButton()

        levelAmount.setFilter(InputHelper::isUIntInput)
        levelAmount.onChanged().subscribe { updateLevelUpButton() }

        val previousPage = rootComponent.childById(ButtonComponent::class, "previous")!!
        val pageCounter = rootComponent.childById(LabelComponent::class, "counter")!!
        val nextPage = rootComponent.childById(ButtonComponent::class, "next")!!
        val exit = rootComponent.childById(ButtonComponent::class, "exit")!!

        PlayerEXMenuRegistry.get().forEach { (_, clazz) ->
            val instance = clazz.getDeclaredConstructor().newInstance()
            instance.init(minecraft!!, this, player.component)
            instance.build(content)
            pages.add(instance)
        }

        this.onLevelUpdated(player.level.toInt())
        this.onPagesUpdated()

        pageCounter.text(Component.nullToEmpty("${currentPage + 1}/${pages.size}"))

        content.clearChildren()
        content.child(pages[currentPage])

        previousPage.onPress {
            if (currentPage > 0) {
                currentPage--
                this.onPagesUpdated()
            }
        }
        nextPage.onPress {
            if (currentPage < pages.lastIndex) {
                currentPage++
                this.onPagesUpdated()
            }
        }

        levelUpButton.onPress {
            levelAmount.value.toIntOrNull()?.let { NetworkingChannels.MODIFY.clientHandle().send(NetworkingPackets.Level(it)) }
        }

        onLevelUpdated.subscribe { this.updateLevelUpButton() }

        exit.onPress { this.onClose() }
    }

    /** Whenever the player's experience is changed, refreshing the current status of experience-tied ui elements. */
    fun onExperienceUpdated() {
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

    enum class AttributeButtonComponentType {
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