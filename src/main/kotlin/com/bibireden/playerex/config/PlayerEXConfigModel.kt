package com.bibireden.playerex.config

import com.bibireden.playerex.PlayerEX
import io.wispforest.owo.config.Option.SyncMode

import io.wispforest.owo.config.annotation.*
import io.wispforest.owo.ui.core.Color

@Suppress("UNUSED")
@Modmenu(modId = PlayerEX.MOD_ID)
@Config(name = "playerex-config", wrapperName = "PlayerEXConfig")
class PlayerEXConfigModel {
    @SectionHeader("server_options")

    @JvmField @Nest @Expanded var levelingSettings = LevelingSettings()
    @JvmField @Nest @Expanded var featureSettings = FeatureSettings()
    @JvmField @Nest @Expanded var lifecycleSettings = LifecycleSettings()
    @JvmField @Nest @Expanded var advancedSettings = AdvancedSettings()
    @JvmField @Nest @Expanded var weaponLevelingSettings = WeaponXpSettings()
    @JvmField @Nest @Expanded var armorLevelingSettings = ArmorXpSettings()

    @SectionHeader("client_options")

    @JvmField @Nest @Expanded var visualSettings = VisualSettings()
    @JvmField @Nest @Expanded var soundSettings = SoundSettings()

    // SERVER

    data class ArmorXpSettings(
        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var enabled: Boolean = true,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var armorPerLevel: Double = 0.1,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var reductionPerLevel: Double = 0.1,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        @RangeConstraint(min = 0.0, max = 25.0)
        var maxReduction: Double = 25.0,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var formula: String = "5x^(1.1)",

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var xpFromPassive: Int = 10,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var xpFromHostile: Int = 20,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var xpFromMiniboss: Int = 50,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var xpFromBoss: Int = 100,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var maxLevel: Int = 500,
    )

    data class WeaponXpSettings(
        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var enabled: Boolean = true,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var damagePerLevel: Double = 0.1,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var formula: String = "5x^(1.1)",

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var xpFromPassive: Int = 10,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var xpFromHostile: Int = 20,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var xpFromMiniboss: Int = 50,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var xpFromBoss: Int = 100,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var maxLevel: Int = 500,
    )

    data class LevelingSettings(
        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var skillPointsPerLevelUp: Int = 1,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        @Hook
        var levelFormula: String = "stairs(x,0.2,2.4,17,10,25)"
    )

    data class FeatureSettings(
        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var resetOnDeath: Boolean = false,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var itemBreakingEnabled: Boolean = true,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var messageOnItemBreak: Boolean = true,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var disableUI: Boolean = false
    )

    data class LifecycleSettings(
        @Sync(SyncMode.OVERRIDE_CLIENT)
        @RestartRequired
        @JvmField
        var healthRegeneration: Lifecycle = Lifecycle.ON_EVERY_SECOND,
    )

    data class AdvancedSettings(
        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var restorativeForceTicks: Int = 600,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var restorativeForceMultiplier: Int = 110,

        @Sync(SyncMode.OVERRIDE_CLIENT)
        @JvmField
        var expNegationFactor: Int = 95
    )

    // CLIENT
    data class VisualSettings(
        @Sync(SyncMode.NONE)
        @JvmField
        var showLevelOnNameplates: Boolean = true,

        @Sync(SyncMode.NONE)
        @JvmField
        var nameplateColor: Color = Color.ofRgb(0xFFAA00),

        @Sync(SyncMode.NONE)
        @JvmField
        var tooltip: Tooltip = Tooltip.Vanilla
    )

    data class SoundSettings(
        @Sync(SyncMode.NONE)
        @JvmField
        @RangeConstraint(min = 0.0, max = 100.0)
        var levelUpVolume: Int = 100,

        @Sync(SyncMode.NONE)
        @JvmField
        @RangeConstraint(min = 0.0, max = 100.0)
        var skillUpVolume: Int = 100,

        @Sync(SyncMode.NONE)
        @JvmField
        @RangeConstraint(min = 0.0, max = 100.0)
        var refundVolume: Int = 100
    )

    enum class Tooltip { Default, Vanilla, PlayerEX }
    enum class Lifecycle { ON_TICK, ON_EVERY_SECOND }
}