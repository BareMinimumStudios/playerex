package com.bibireden.playerex.config

import me.fzzyhmstrs.fzzy_config.api.ConfigApi

object Configs {
    private lateinit var magicConfig: com.bibireden.playerex.compat.iron.IronSpellsConfig
    lateinit var CONFIG: PlayerExConfig
        private set

    @JvmStatic
    fun init() {
        if (::CONFIG.isInitialized) return
        CONFIG = ConfigApi.registerAndLoadConfig(::PlayerExConfig)
        PlayerExConfigState.replace(CONFIG.toSnapshot())
        magicConfig = ConfigApi.registerAndLoadConfig({ com.bibireden.playerex.compat.iron.IronSpellsConfig() })
        magicConfig.publish()
    }
}
