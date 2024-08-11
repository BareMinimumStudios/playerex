package com.bibireden.playerex.config

import com.bibireden.playerex.PlayerEX
import io.wispforest.owo.config.Option.SyncMode

import io.wispforest.owo.config.annotation.*
import io.wispforest.owo.ui.core.Color

@Suppress("UNUSED")
@Modmenu(modId = PlayerEX.MOD_ID)
@Config(name = "playerex-config", wrapperName = "PlayerEXConfig")
class PlayerEXConfigModel {
    @SectionHeader("client_options")

    @Sync(SyncMode.NONE)
    @JvmField
    var tooltip: Tooltip = Tooltip.Vanilla

    @Sync(SyncMode.NONE)
    @JvmField
    var showLevelOnNameplates: Boolean = true

    data class SoundSettings(
        @Sync(SyncMode.NONE)
        @JvmField
        @RangeConstraint(min = 0.0, max = 150.0)
        var levelUpVolume: Int = 100,

        @Sync(SyncMode.NONE)
        @JvmField
        @RangeConstraint(min = 0.0, max = 150.0)
        var skillUpVolume: Int = 100,

        @Sync(SyncMode.NONE)
        @JvmField
        @RangeConstraint(min = 0.0, max = 150.0)
        var refundVolume: Int = 100
    )

    @JvmField @Nest var soundSettings = SoundSettings()

    data class VisualSettings(
        @Sync(SyncMode.NONE)
        @JvmField
        var nameplateColor: Color = Color.ofRgb(0xFFAA00),
    )

    @JvmField @Nest var visualSettings = VisualSettings()

    @SectionHeader("server_options")
    @Sync(SyncMode.OVERRIDE_CLIENT)
    @JvmField
    var resetOnDeath: Boolean = false

    @Sync(SyncMode.OVERRIDE_CLIENT)
    @JvmField
    var disableUI: Boolean = false

    @Sync(SyncMode.OVERRIDE_CLIENT)
    @JvmField
    var skillPointsPerLevelUp: Int = 1

    @Sync(SyncMode.OVERRIDE_CLIENT)
    @JvmField
    @Hook
    var levelFormula: String = "stairs(x,0.2,2.4,17,10,25)"

//    @JvmField
//    var expression: Expression

    @Sync(SyncMode.OVERRIDE_CLIENT)
    @JvmField
    var restorativeForceTicks: Int = 600

    @Sync(SyncMode.OVERRIDE_CLIENT)
    @JvmField
    var restorativeForceMultiplier: Int = 110

    @Sync(SyncMode.OVERRIDE_CLIENT)
    @JvmField
    var expNegationFactor: Int = 95

    enum class Tooltip { Default, Vanilla, PlayerEX }
}