package com.bibireden.playerex.ui.components

import com.bibireden.playerex.components.player.IPlayerDataComponent
import com.bibireden.playerex.ui.PlayerEXScreen
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.util.EventSource
import io.wispforest.owo.util.EventStream
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.attribute.EntityAttribute
import org.jetbrains.annotations.ApiStatus

/**
 * A component meant to be used in the **PlayerEX** screen.
 *
 * This allows for other mods to create their own custom logic,
 * and have the benefits of a unique instance that is attached to the primary mod.
 */
@ApiStatus.OverrideOnly
abstract class MenuComponent(horizontalSizing: Sizing, verticalSizing: Sizing, algorithm: Algorithm) : FlowLayout(horizontalSizing, verticalSizing, algorithm) {
    val onLevelUpdatedEvents = OnLevelUpdated.stream
    val onAttributeUpdatedEvents = OnAttributeUpdated.stream

    val onLevelUpdated: EventSource<OnLevelUpdated> = onLevelUpdatedEvents.source()
    val onAttributeUpdated: EventSource<OnAttributeUpdated> = onAttributeUpdatedEvents.source()

    /** When the [PlayerEXScreen] is ready to be constructed, this function (if the component is registered) will be called.*/
    abstract fun build(player: ClientPlayerEntity, adapter: OwoUIAdapter<FlowLayout>, component: IPlayerDataComponent)

    fun interface OnLevelUpdated {
        fun onLevelUpdated(level: Int)

        companion object {
            val stream: EventStream<OnLevelUpdated> get() = EventStream { subscribers ->
                OnLevelUpdated { level -> subscribers.forEach { it.onLevelUpdated(level) } }
            }
        }
    }

    fun interface OnAttributeUpdated {
        fun onAttributeUpdated(attribute: EntityAttribute, level: Double)

        companion object {
            val stream: EventStream<OnAttributeUpdated> get() = EventStream { subscribers ->
                OnAttributeUpdated {  attribute, value -> subscribers.forEach { it.onAttributeUpdated(attribute, value) } }
            }
        }
    }
}