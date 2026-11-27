package com.bibireden.playerex.config

import com.bibireden.playerex.compat.iron.IronSpellsConfig
import me.fzzyhmstrs.fzzy_config.api.ConfigApi

object Configs {
    private lateinit var magicConfig: IronSpellsConfig
    lateinit var CONFIG: PlayerExConfig
        private set

    @JvmStatic
    fun init() {
        if (::CONFIG.isInitialized) return
        CONFIG = ConfigApi.registerAndLoadConfig(::PlayerExConfig)
        PlayerExConfigState.replace(CONFIG.toSnapshot())
        magicConfig = ConfigApi.registerAndLoadConfig({ IronSpellsConfig() })
        magicConfig.publish()
    }
}
