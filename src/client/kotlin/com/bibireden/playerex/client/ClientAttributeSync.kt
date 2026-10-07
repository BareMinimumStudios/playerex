package com.bibireden.playerex.client

import com.bibireden.playerex.PlayerEX
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.LivingEntity

/** Briefly retain vanilla server snapshots while Data Attributes rebuilds the client supplier.
 * Replay the original packet, including equipment/base values, through Minecraft's handler.
 * No allocation is predicted and no replacement stat is calculated here.
 */
object ClientAttributeSync {
    private data class Pending(val packet: ClientboundUpdateAttributesPacket, var remaining: Int = 40)
    private val pending = ArrayDeque<Pending>()
    private var level: ClientLevel? = null
    private var replaying = false

    private fun currentLevel(): ClientLevel? {
        val current = Minecraft.getInstance().level
        if (current !== level) { pending.clear(); level = current }
        return current
    }
    private fun missing(packet: ClientboundUpdateAttributesPacket, world: ClientLevel): Boolean {
        val entity = world.getEntity(packet.entityId) as? LivingEntity ?: return false
        return packet.values.any { snapshot ->
            BuiltInRegistries.ATTRIBUTE.getKey(snapshot.attribute().value())?.namespace == PlayerEX.MOD_ID &&
                entity.getAttribute(snapshot.attribute()) == null
        }
    }

    /** Called on the client thread, after vanilla's packet thread guard. */
    @JvmStatic
    fun defer(packet: ClientboundUpdateAttributesPacket): Boolean {
        if (replaying) return false
        val world = currentLevel() ?: return false
        if (!pending.any { it.packet.entityId == packet.entityId } && !missing(packet, world)) return false
        if (pending.size >= 64) {
            pending.removeFirst()
            PlayerEX.LOGGER.warn("Client attribute sync queue reached its bound; discarding oldest snapshot")
        }
        pending.addLast(Pending(packet))
        return true
    }

    fun tick() {
        val world = currentLevel() ?: return
        val connection = Minecraft.getInstance().connection ?: return
        // Preserve arrival order, including newer snapshots received while an older one is waiting.
        repeat(pending.size) {
            val entry = pending.removeFirst()
            if (missing(entry.packet, world) && --entry.remaining > 0) {
                pending.addLast(entry)
            } else {
                if (entry.remaining <= 0) PlayerEX.LOGGER.warn("Client attribute container still missing PlayerEx attributes for entity {}; applying vanilla fallback", entry.packet.entityId)
                replaying = true
                try { connection.handleUpdateAttributes(entry.packet) } finally { replaying = false }
            }
        }
    }
}
