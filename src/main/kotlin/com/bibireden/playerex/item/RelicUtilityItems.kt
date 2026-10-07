package com.bibireden.playerex.item

import com.bibireden.playerex.progression.PlayerProgression
import com.bibireden.playerex.state.PlayerExState
import com.bibireden.playerex.state.PlayerStateService
import com.bibireden.playerex.state.PlayerAttributeReconciler
import net.minecraft.ChatFormatting
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

/** Built-in RelicEx utility content; progression changes remain server-owned. */
object RelicUtilityItems {
    val factories: Map<String, () -> Item> = linkedMapOf(
        "tome" to { UtilityItem(Kind.TOME, Rarity.UNCOMMON) },
        "lesser_orb_of_regret" to { UtilityItem(Kind.LESSER_ORB, Rarity.RARE) },
        "greater_orb_of_regret" to { UtilityItem(Kind.GREATER_ORB, Rarity.EPIC) },
        "dragon_stone" to { UtilityItem(Kind.DRAGON_STONE, Rarity.EPIC) },
        "relic_shard" to { UtilityItem(Kind.SHARD, Rarity.UNCOMMON) },
        "small_health_potion" to { HealthPotionItem(4f) },
        "medium_health_potion" to { HealthPotionItem(6f) },
        "large_health_potion" to { HealthPotionItem(8f) }
    )

    private enum class Kind { TOME, LESSER_ORB, GREATER_ORB, DRAGON_STONE, SHARD }

    private class UtilityItem(private val kind: Kind, rarity: Rarity) : Item(
        Properties().rarity(rarity).apply {
            if (kind != Kind.TOME && kind != Kind.SHARD) stacksTo(1)
            if (kind == Kind.SHARD) fireResistant()
        }
    ) {
        override fun isFoil(stack: ItemStack): Boolean = kind == Kind.SHARD || super.isFoil(stack)

        override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Component>, flag: TooltipFlag) {
            val key = when (kind) {
                Kind.TOME -> "tome"
                Kind.LESSER_ORB -> "lesser_orb_of_regret"
                Kind.GREATER_ORB -> "greater_orb_of_regret"
                Kind.DRAGON_STONE -> "dragon_stone"
                Kind.SHARD -> "relic_shard"
            }
            tooltip += Component.translatable("tooltip.playerex.$key").withStyle(ChatFormatting.GRAY)
        }

        override fun use(level: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
            val stack = user.getItemInHand(hand)
            if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true)
            val player = user as? ServerPlayer ?: return InteractionResultHolder.pass(stack)
            var consume = true
            val success = when (kind) {
                Kind.TOME -> PlayerProgression.grantLevel(player, 1)
                Kind.LESSER_ORB -> PlayerProgression.grantRefundPoints(player, 1)
                Kind.GREATER_ORB -> PlayerProgression.grantRefundPoints(player, Int.MAX_VALUE)
                Kind.DRAGON_STONE -> {
                    player.cooldowns.addCooldown(this, 20)
                    val state = PlayerStateService.get(player)
                    val previous = stack.getOrDefault(PlayerExItemComponents.DRAGON_STONE_USERS, emptyList())
                    if (state.level <= 0) false
                    else if (player.uuid !in previous) {
                        consume = false
                        if (previous.size >= DragonStoneUsers.MAX_USERS) false
                        else {
                            stack.set(PlayerExItemComponents.DRAGON_STONE_USERS, previous + player.uuid)
                            player.displayClientMessage(Component.translatable("message.playerex.dragon_stone"), true)
                            true
                        }
                    } else {
                        PlayerStateService.set(player, PlayerExState.EMPTY, false)
                        PlayerAttributeReconciler.reconcile(player)
                        player.health = player.health.coerceAtMost(player.maxHealth)
                        PlayerStateService.sync(player)
                        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.75f, 1f)
                        true
                    }
                }
                Kind.SHARD -> {
                    ExperienceOrb.award(player.serverLevel(), player.position(), 3 + player.random.nextInt(5) + player.random.nextInt(5))
                    level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1f, 1f)
                    true
                }
            }
            if (!success) return InteractionResultHolder.fail(stack)
            if (consume && !player.abilities.instabuild) stack.shrink(1)
            return InteractionResultHolder.sidedSuccess(stack, false)
        }
    }

    // Original RelicEx health potions activate automatically on entering a living entity's inventory.
    private class HealthPotionItem(private val amount: Float) : Item(Properties().stacksTo(1)) {
        override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slot: Int, selected: Boolean) {
            if (level !is ServerLevel || entity !is LivingEntity || stack.isEmpty) return
            entity.heal(amount)
            stack.shrink(1)
            level.sendParticles(ParticleTypes.HEART, entity.x, entity.y + 0.5, entity.z, 6, 0.5, 0.5, 0.5, 0.02)
            level.playSound(null, entity.blockPosition(), com.bibireden.playerex.api.event.PlayerEXSoundEvents.POTION_USE_SOUND, SoundSource.PLAYERS, 0.75f, 1f)
        }
    }
}
