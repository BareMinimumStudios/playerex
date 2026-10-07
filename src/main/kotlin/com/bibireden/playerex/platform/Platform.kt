package com.bibireden.playerex.platform

/** Loader bridge. No loader API is allowed in common code. */
object Platform {
    private var modLoaded: (String) -> Boolean = { false }

    fun installModLookup(lookup: (String) -> Boolean) {
        modLoaded = lookup
    }

    fun isModLoaded(modId: String): Boolean = modLoaded(modId)
}
