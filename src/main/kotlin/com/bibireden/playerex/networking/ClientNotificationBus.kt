package com.bibireden.playerex.networking

import com.bibireden.playerex.networking.type.NotificationType
import java.util.concurrent.CopyOnWriteArrayList

/** Client-only consumers (GUI/sound layer in Step 4) can subscribe without loader networking leaking into UI code. */
object ClientNotificationBus {
    private val listeners = CopyOnWriteArrayList<(NotificationType) -> Unit>()

    @Volatile
    private var last: NotificationType? = null

    fun latest(): NotificationType? = last

    fun receive(type: NotificationType) {
        last = type
        listeners.forEach { listener -> listener(type) }
    }

    fun listen(listener: (NotificationType) -> Unit): AutoCloseable {
        listeners += listener
        return AutoCloseable { listeners -= listener }
    }

    fun clear() {
        last = null
    }
}
