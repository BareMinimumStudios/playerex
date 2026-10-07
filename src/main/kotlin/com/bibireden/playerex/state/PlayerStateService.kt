package com.bibireden.playerex.state

import com.bibireden.playerex.networking.PlatformNetworking
import com.bibireden.playerex.networking.payload.PlayerStateSyncPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.ChunkAccess

/** Single mutation/sync point for persisted PlayerEx state. */
object PlayerStateService {
    @JvmStatic
    fun get(player: Player): PlayerExState = PlatformStateStorage.player(player)

    @JvmStatic
    fun set(player: Player, state: PlayerExState, sync: Boolean = true): PlayerExState {
        val normalized = state.normalized()
        PlatformStateStorage.setPlayer(player, normalized)
        if (sync && player is ServerPlayer) PlatformNetworking.send(player, PlayerStateSyncPayload(normalized))
        return normalized
    }

    @JvmStatic
    fun mutate(player: Player, sync: Boolean = true, transform: (PlayerExState) -> PlayerExState): PlayerExState =
        set(player, transform(get(player)), sync)

    @JvmStatic
    fun sync(player: ServerPlayer) {
        PlatformNetworking.send(player, PlayerStateSyncPayload(get(player)))
    }

    @JvmStatic
    fun chunk(chunk: ChunkAccess): ChunkExperienceState = PlatformStateStorage.chunk(chunk)

    @JvmStatic
    fun setChunk(chunk: ChunkAccess, state: ChunkExperienceState): ChunkExperienceState {
        val normalized = state.normalized()
        PlatformStateStorage.setChunk(chunk, normalized)
        return normalized
    }
}
