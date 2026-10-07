package com.bibireden.playerex.item

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.config.PlayerExConfigState
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import java.util.function.Consumer

object RelicLoot {
    private val potions = listOf("small_health_potion", "medium_health_potion", "large_health_potion")
    private fun item(name: String) = ItemStack(BuiltInRegistries.ITEM.get(PlayerEX.id(name)))
    fun relic(level: ServerLevel, random: RandomSource, dimensionRules: Boolean = true): ItemStack? {
        val config = PlayerExConfigState.current()
        val rules = if (dimensionRules && config.relicDimensionRulesEnabled) config.relicDimensionRules[level.dimension().location().toString()] else null
        val names = RelicEquipment.factories.keys.toList()
        repeat(if (rules == null) 1 else 32) {
            val state = RelicWeights.roll(random) ?: return null
            if (rules == null || random.nextInt(100) < rules[state.rarity + 1])
                return item(names[random.nextInt(names.size)]).apply { set(PlayerExItemComponents.RELIC, state) }
        }
        return null
    }
    @JvmStatic fun chest(context: LootContext, consumer: Consumer<ItemStack>) {
        val config = PlayerExConfigState.current()
        if (!config.relicChestsHaveLoot) return
        val random = context.random
        if (random.nextFloat() < config.relicChestChance / 100f) relic(context.level, random, false)?.let(consumer::accept)
        for ((name, chance) in listOf("lesser_orb_of_regret" to config.relicChestLesserOrbChance,
            "greater_orb_of_regret" to config.relicChestGreaterOrbChance, "tome" to config.relicChestTomeChance)) {
            if (random.nextFloat() < chance / 100f) consumer.accept(item(name))
        }
    }
    @JvmStatic fun mob(victim: LivingEntity, source: DamageSource, causedByPlayer: Boolean) {
        val level = victim.level() as? ServerLevel ?: return
        val config = PlayerExConfigState.current()
        val id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.type).toString()
        if (id == "minecraft:ender_dragon") {
            if (config.relicDragonDropsStone) victim.spawnAtLocation(item("dragon_stone"))
            return
        }
        if (victim !is Enemy || id in config.relicMobBlacklist || (config.relicOnlyPlayerKills && !causedByPlayer)) return
        // Mob killers such as iron golems do not necessarily have a Luck attribute.
        val luck = (source.entity as? LivingEntity)?.getAttribute(Attributes.LUCK)?.value ?: 0.0
        val multiplier = if (config.relicDimensionRulesEnabled) (config.relicDimensionRules[level.dimension().location().toString()]?.firstOrNull() ?: 100) / 100.0 else 1.0
        if (victim.random.nextDouble() >= (config.relicMobLootChance / 100.0 * multiplier * (1 + luck)).coerceIn(0.0, 1.0)) return
        val choices = listOf("relic" to config.relicMobRelicWeight, "potion" to config.relicMobPotionWeight,
            "lesser_orb_of_regret" to config.relicMobLesserOrbWeight, "greater_orb_of_regret" to config.relicMobGreaterOrbWeight,
            "tome" to config.relicMobTomeWeight)
        val sum = choices.sumOf { it.second }
        if (sum == 0) return
        var roll = victim.random.nextInt(sum)
        for ((name, weight) in choices) {
            roll -= weight
            if (roll >= 0) continue
            val stack = when (name) {
                "relic" -> relic(level, victim.random)
                "potion" -> item(potions[victim.random.nextInt(potions.size)])
                else -> item(name)
            }
            if (stack != null) victim.spawnAtLocation(stack)
            return
        }
    }
}
