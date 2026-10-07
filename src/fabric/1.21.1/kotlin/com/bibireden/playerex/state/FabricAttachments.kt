package com.bibireden.playerex.state

import com.bibireden.playerex.PlayerEX
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType

object FabricAttachments {
    private val PLAYER_STATE: AttachmentType<PlayerExState> = AttachmentRegistry.create(PlayerEX.id("player_state")) { builder ->
        builder
            .initializer { PlayerExState.EMPTY }
            .persistent(PlayerExState.CODEC)
            .copyOnDeath()
    }

    private val CHUNK_EXPERIENCE: AttachmentType<ChunkExperienceState> = AttachmentRegistry.create(PlayerEX.id("chunk_experience")) { builder ->
        builder
            .initializer { ChunkExperienceState.EMPTY }
            .persistent(ChunkExperienceState.CODEC)
    }

    fun init() {
        PlatformStateStorage.install(
            playerGetter = { player -> (player as AttachmentTarget).getAttachedOrCreate(PLAYER_STATE) },
            playerSetter = { player, state -> (player as AttachmentTarget).setAttached(PLAYER_STATE, state) },
            chunkGetter = { chunk -> (chunk as AttachmentTarget).getAttachedOrCreate(CHUNK_EXPERIENCE) },
            chunkSetter = { chunk, state -> (chunk as AttachmentTarget).setAttached(CHUNK_EXPERIENCE, state) }
        )
    }
}
