package com.bibireden.playerex.ui.menus

import com.bibireden.playerex.components.player.IPlayerDataComponent
import com.bibireden.playerex.ui.components.MenuComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import net.minecraft.client.player.LocalPlayer
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class ConceptMenu : MenuComponent(algorithm = Algorithm.VERTICAL) {
    override fun build(player: LocalPlayer, adapter: OwoUIAdapter<FlowLayout>, component: IPlayerDataComponent) {
    }
}