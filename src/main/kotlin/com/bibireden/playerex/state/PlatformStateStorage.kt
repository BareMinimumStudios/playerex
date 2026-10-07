package com.bibireden.playerex.state

import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.ChunkAccess

/**
 * Loader bridge for Fabric/NeoForge attachment implementations.
 * Common code owns the data model; loaders own only attachment registration/access.
 */
object PlatformStateStorage {
    private var getPlayerState: ((Player) -> PlayerExState)? = null
    private var setPlayerState: ((Player, PlayerExState) -> Unit)? = null
    private var getChunkState: ((ChunkAccess) -> ChunkExperienceState)? = null
    private var setChunkState: ((ChunkAccess, ChunkExperienceState) -> Unit)? = null

    fun install(
        playerGetter: (Player) -> PlayerExState,
        playerSetter: (Player, PlayerExState) -> Unit,
        chunkGetter: (ChunkAccess) -> ChunkExperienceState,
        chunkSetter: (ChunkAccess, ChunkExperienceState) -> Unit
    ) {
        getPlayerState = playerGetter
        setPlayerState = playerSetter
        getChunkState = chunkGetter
        setChunkState = chunkSetter
    }

    fun player(player: Player): PlayerExState =
        getPlayerState?.invoke(player) ?: error("PlayerEx platform state storage is not initialized")

    fun setPlayer(player: Player, state: PlayerExState) {
        (setPlayerState ?: error("PlayerEx platform state storage is not initialized"))(player, state.normalized())
    }

    fun chunk(chunk: ChunkAccess): ChunkExperienceState =
        getChunkState?.invoke(chunk) ?: error("PlayerEx platform state storage is not initialized")

    fun setChunk(chunk: ChunkAccess, state: ChunkExperienceState) {
        (setChunkState ?: error("PlayerEx platform state storage is not initialized"))(chunk, state.normalized())
    }
}
