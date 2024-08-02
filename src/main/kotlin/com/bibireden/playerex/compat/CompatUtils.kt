package com.bibireden.playerex.compat

import net.fabricmc.loader.api.FabricLoader
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object CompatUtils {
    fun isModLoaded(id: String): Boolean = FabricLoader.getInstance().isModLoaded(id)
}