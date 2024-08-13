package com.bibireden.playerex.ui.menus

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.EntityAttributeSupplier
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.components.player.IPlayerDataComponent
import com.bibireden.playerex.ext.id
import com.bibireden.playerex.ext.level
import com.bibireden.playerex.ui.PlayerEXScreen
import com.bibireden.playerex.ui.childById
import com.bibireden.playerex.ui.components.*
import com.bibireden.playerex.ui.components.buttons.AttributeButtonComponent
import com.bibireden.playerex.ui.components.labels.AttributeLabelComponent
import com.bibireden.playerex.ui.helper.InputHelper
import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class PlayerEXAttributesMenu : MenuComponent(algorithm = Algorithm.HORIZONTAL) {
    private val MELEE_COMBAT_STATS: List<EntityAttributeSupplier> = listOf(
        EntityAttributeSupplier(Attributes.ATTACK_DAMAGE.id),
        EntityAttributeSupplier(Attributes.ATTACK_SPEED.id),
        EntityAttributeSupplier(PlayerEXAttributes.MELEE_CRITICAL_DAMAGE.id),
        EntityAttributeSupplier(PlayerEXAttributes.MELEE_CRITICAL_CHANCE.id)
    )

    private val RANGED_COMBAT_STATS: List<EntityAttributeSupplier> = listOf(
        EntityAttributeSupplier(PlayerEXAttributes.RANGED_CRITICAL_DAMAGE.id),
        EntityAttributeSupplier(PlayerEXAttributes.RANGED_CRITICAL_CHANCE.id),
        EntityAttributeSupplier(EntityAttributes_RangedWeapon.HASTE.id),
        EntityAttributeSupplier(EntityAttributes_RangedWeapon.DAMAGE.id),
    )

    private val DEFENSE_COMBAT_STATS: List<EntityAttributeSupplier> = listOf(
        EntityAttributeSupplier(Attributes.ARMOR.id),
        EntityAttributeSupplier(AdditionalEntityAttributes.MAGIC_PROTECTION.id),
        EntityAttributeSupplier(Attributes.ARMOR_TOUGHNESS.id),
        EntityAttributeSupplier(Attributes.KNOCKBACK_RESISTANCE.id),
        EntityAttributeSupplier(PlayerEXAttributes.EVASION.id),
    )

    private val VITALITY_STATS: List<EntityAttributeSupplier> = listOf(
        EntityAttributeSupplier(PlayerEXAttributes.HEALTH_REGENERATION.id),
        EntityAttributeSupplier(PlayerEXAttributes.HEAL_AMPLIFICATION.id),
        EntityAttributeSupplier(PlayerEXAttributes.LIFESTEAL.id),
        EntityAttributeSupplier(Attributes.MOVEMENT_SPEED.id),
    )

    private val RESISTANCE_STATS: List<EntityAttributeSupplier> = listOf(
        EntityAttributeSupplier(PlayerEXAttributes.FIRE_RESISTANCE.id),
        EntityAttributeSupplier(PlayerEXAttributes.FREEZE_RESISTANCE.id),
        EntityAttributeSupplier(PlayerEXAttributes.LIGHTNING_RESISTANCE.id),
        EntityAttributeSupplier(PlayerEXAttributes.POISON_RESISTANCE.id),
    )

    /** Whenever ANY attribute gets updated. */
    private fun onAttributeUpdate() {
        // refresh all attribute labels
        this.forEachDescendant { component ->
            // a bit more tolerable
            when (component) {
                is AttributeComponent -> component.refresh()
                is AttributeLabelComponent -> component.refresh()
                is AttributeListEntryComponent -> component.refresh()
            }
        }
    }

    private fun onInputFieldUpdated(player: Player, component: IPlayerDataComponent) {
        this.childById(FlowLayout::class, "attributes")?.childById(TextBoxComponent::class, "input")?.also {
            val result = it.value.toDoubleOrNull() ?: return@also
            this.forEachDescendant { descendant ->
                if (descendant is AttributeButtonComponent) {
                    val max = descendant.attribute.`data_attributes$max`()
                    val current = DataAttributesAPI.getValue(descendant.attribute, player).orElse(0.0)
                    when (descendant.type) {
                        PlayerEXScreen.AttributeButtonComponentType.Add -> descendant.active(result > 0 && component.skillPoints >= result && (current + result) <= max)
                        PlayerEXScreen.AttributeButtonComponentType.Remove -> descendant.active(result > 0 && component.refundablePoints > 0 && (current - result >= 0))
                    }
                }
            }
        }

    }

    override fun build(screenRoot: FlowLayout) {
        val player = client?.player ?: return
        val component = playerComponent ?: return

        child(Containers.verticalScroll(
            Sizing.fill(45),
            Sizing.fill(100),
            Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content(2)).apply {
                    child(Components.label(Component.translatable("playerex.ui.category.primary_attributes")))
                    child(
                        Components.textBox(Sizing.fixed(27))
                            .text("1")
                            .also {
                                it.setMaxLength(4)
                                it.setFilter(InputHelper::isUIntInput)
                                it.onChanged().subscribe { onInputFieldUpdated(player, component) }
                            }
                            .verticalSizing(Sizing.fixed(10))
                            .positioning(Positioning.relative(100, 0))
                            .id("input")
                    )
                })
                child(Components.box(Sizing.fill(100), Sizing.fixed(2)))
                verticalAlignment(VerticalAlignment.TOP)
                gap(5)
                padding(Insets.right(5))
                children(PlayerEXAttributes.PRIMARY_ATTRIBUTE_IDS.mapNotNull(BuiltInRegistries.ATTRIBUTE::get).map { AttributeComponent(it, player, component) })
            }.id("attributes"))
        )
        child(Containers.verticalScroll(
            Sizing.content(),
            Sizing.fill(100),
            Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                child(AttributeListComponent("playerex.ui.main.categories.vitality", player, VITALITY_STATS))
                child(AttributeListComponent("playerex.ui.main.categories.resistance", player, RESISTANCE_STATS))
                padding(Insets.right(5))
                gap(8)
            }
        ))

        child(
            Containers.verticalScroll(
                Sizing.content(),
                Sizing.fill(100),
                Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    child(AttributeListComponent("playerex.ui.main.categories.melee_combat", player, MELEE_COMBAT_STATS))
                    child(AttributeListComponent("playerex.ui.main.categories.ranged_combat", player, RANGED_COMBAT_STATS))
                    child(AttributeListComponent("playerex.ui.main.categories.defense_combat", player, DEFENSE_COMBAT_STATS))
                    padding(Insets.right(5))
                    gap(10)
                }.id("combat_stats")
            )
        )

        gap(10)

        padding(Insets.both(8, 8))

        onAttributeUpdate()
        onInputFieldUpdated(player, component)

        onAttributeUpdated.subscribe { _, _ ->
            onAttributeUpdate()
            onInputFieldUpdated(player, component)
        }
    }
}