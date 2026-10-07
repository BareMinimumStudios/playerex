package com.bibireden.playerex.config

import me.fzzyhmstrs.fzzy_config.api.ConfigApi

object Configs {
    lateinit var CONFIG: PlayerExConfig
        private set

    @JvmStatic
    fun init() {
        if (::CONFIG.isInitialized) return
        CONFIG = ConfigApi.registerAndLoadConfig(::PlayerExConfig)
        PlayerExConfigState.replace(CONFIG.toSnapshot())
    }
}
