package com.bibireden.playerex.state

import com.bibireden.playerex.PlayerEX
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

object NeoForgeAttachments {
    private val ATTACHMENTS: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PlayerEX.MOD_ID)

    private val PLAYER_STATE = ATTACHMENTS.register(
        "player_state",
        Supplier<AttachmentType<PlayerExState>> {
            AttachmentType.builder(Supplier { PlayerExState.EMPTY })
                .serialize(PlayerExState.CODEC)
                .copyOnDeath()
                .build()
        }
    )

    private val CHUNK_EXPERIENCE = ATTACHMENTS.register(
        "chunk_experience",
        Supplier<AttachmentType<ChunkExperienceState>> {
            AttachmentType.builder(Supplier { ChunkExperienceState.EMPTY })
                .serialize(ChunkExperienceState.CODEC)
                .build()
        }
    )

    fun init(modBus: IEventBus) {
        ATTACHMENTS.register(modBus)
        PlatformStateStorage.install(
            playerGetter = { player -> player.getData(PLAYER_STATE.get()) },
            playerSetter = { player, state -> player.setData(PLAYER_STATE.get(), state) },
            chunkGetter = { chunk -> chunk.getData(CHUNK_EXPERIENCE.get()) },
            chunkSetter = { chunk, state -> chunk.setData(CHUNK_EXPERIENCE.get(), state) }
        )
    }
}
