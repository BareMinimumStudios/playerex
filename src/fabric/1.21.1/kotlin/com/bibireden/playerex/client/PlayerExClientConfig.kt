package com.bibireden.playerex.client
import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.item.ItemProgressionTooltip
import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.api.RegisterType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup
import me.fzzyhmstrs.fzzy_config.util.Translatable

@Version(1)
@RootConfig
@Translatable.Name("PlayerEx client")
class PlayerExClientConfig : Config(PlayerEX.id("client_sounds")) {
    @Translatable.Name("Notification sounds") val soundsGroup = ConfigGroup("sounds", true)
    @Translatable.Name("Level notification volume") var levelUpVolume: Int = 100
    @Translatable.Name("Skill allocation volume") var skillUpVolume: Int = 100
    @ConfigGroup.Pop
    @Translatable.Name("Refund volume") var refundVolume: Int = 100
    @Translatable.Name("Player nameplates") val nameplatesGroup = ConfigGroup("nameplates", true)
    @Translatable.Name("Show levels on player nameplates") var showLevelOnNameplates: Boolean = true
    @ConfigGroup.Pop
    @Translatable.Name("Nameplate level color (RGB)") var nameplateColor: Int = 0xFFAA00
    @Translatable.Name("Item tooltips") val tooltipsGroup = ConfigGroup("tooltips", true)
    @Translatable.Name("Equipment tooltips") var equipmentTooltips: EquipmentTooltipPresentation.Mode = EquipmentTooltipPresentation.Mode.Vanilla
    @ConfigGroup.Pop
    @Translatable.Name("Hold Shift for item level details") var holdShiftForItemLevels: Boolean = false
    @Translatable.Name("Advanced: tooltip colors") val colorsGroup = ConfigGroup("colors", true)
    @Translatable.Name("Item level title color (RGB)") var itemTitleColor: Int = 0xE3C46D
    @Translatable.Name("Item level arrow color (RGB)") var itemArrowColor: Int = 0xE3C46D
    @Translatable.Name("Item level text color (RGB)") var itemTextColor: Int = 0xAAA99F
    @Translatable.Name("Item level value color (RGB)") var itemValueColor: Int = 0xE8E5DA
    @ConfigGroup.Pop
    @Translatable.Name("Item level Shift hint color (RGB)") var itemShiftColor: Int = 0xE3C46D
    override fun update(deserializedVersion: Int) {
        if (deserializedVersion < 1 && itemTitleColor == 12517240 && itemArrowColor == 12517240 && itemTextColor == 9736850 && itemValueColor == 15422034 && itemShiftColor == 12517240) {
            itemTitleColor = 0xE3C46D
            itemArrowColor = 0xE3C46D
            itemTextColor = 0xAAA99F
            itemValueColor = 0xE8E5DA
            itemShiftColor = 0xE3C46D
        }
    }
    override fun onUpdateClient() { publish() }
    private fun publish() {
        EquipmentTooltipPresentation.holdShift = holdShiftForItemLevels
        ItemProgressionTooltip.configure(
            ItemProgressionTooltip.Colors(itemTitleColor, itemArrowColor, itemTextColor, itemValueColor, itemShiftColor)
        )
        EquipmentTooltipPresentation.configure(equipmentTooltips)
        PlayerExNameplates.configure(showLevelOnNameplates, nameplateColor)
        PlayerExSoundSettings.replace(PlayerExSoundSettings.Values(levelUpVolume, skillUpVolume, refundVolume))
    }
    companion object {
        private var initialized = false
        fun init() {
            if (initialized) return
            ConfigApi.registerAndLoadConfig(::PlayerExClientConfig, RegisterType.CLIENT).publish()
            initialized = true
        }
    }
}
