package com.bibireden.playerex.client

import com.bibireden.playerex.platform.Platform
import net.minecraft.world.entity.player.Player

/** Read-only view of Mana Attributes' synchronized state; never predicts casting costs. */
object ManaPresentation {
    val available: Boolean by lazy { Platform.isModLoaded("fabricloader") && Platform.isModLoaded("manaattributes") }
    private val api by lazy {
        Class.forName("com.github.theredbrain.manaattributes.entity.ManaUsingEntity").let {
            it.getMethod("manaattributes\$getMana") to it.getMethod("manaattributes\$getUnreservedMana")
        }
    }
    data class Value(val current: Float, val maximum: Float)
    fun value(player: Player): Value? {
        if (!available) return null
        val current = (api.first.invoke(player) as Number).toFloat()
        val maximum = (api.second.invoke(player) as Number).toFloat()
        if (!current.isFinite() || !maximum.isFinite() || maximum < 0) return null
        return Value(current.coerceIn(0f, maximum), maximum)
    }
}
