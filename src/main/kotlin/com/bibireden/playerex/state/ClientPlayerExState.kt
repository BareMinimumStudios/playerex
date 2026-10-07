package com.bibireden.playerex.state

/** Client-side snapshot populated only by PlayerEx's explicit state-sync payload. */
object ClientPlayerExState {
    @Volatile
    private var state: PlayerExState = PlayerExState.EMPTY

    @JvmStatic
    fun current(): PlayerExState = state

    @JvmStatic
    fun replace(value: PlayerExState) {
        state = value.normalized()
    }

    @JvmStatic
    fun clear() {
        state = PlayerExState.EMPTY
    }
}
