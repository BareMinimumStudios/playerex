package com.bibireden.playerex.client
import com.bibireden.playerex.networking.type.NotificationType

object PlayerExSoundSettings {
    data class Values(val levelUp: Int = 100, val spend: Int = 100, val refund: Int = 100)
    private var values = Values()
    fun replace(candidate: Values) { values = Values(candidate.levelUp.coerceIn(0, 100), candidate.spend.coerceIn(0, 100), candidate.refund.coerceIn(0, 100)) }
    fun volume(notification: NotificationType): Float = when (notification) {
        NotificationType.LEVEL_UP_AVAILABLE -> values.levelUp
        NotificationType.SPENT -> values.spend
        NotificationType.REFUNDED -> values.refund
    } / 100f
}
