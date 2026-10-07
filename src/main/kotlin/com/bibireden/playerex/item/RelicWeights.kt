package com.bibireden.playerex.item

import com.bibireden.playerex.PlayerEX
import com.google.gson.Gson
import com.google.gson.JsonElement
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.util.RandomSource
import kotlin.random.Random

object RelicWeights {
    private val gson = Gson()
    private var pool = RelicRollMath.Pool(emptyMap())
    fun roll(random: RandomSource): RelicState? = pool.roll(Random(random.nextLong()))?.let { rolled ->
        RelicState(rolled.rarity, rolled.attributes.map { RelicAttribute(ResourceLocation.parse(it.attribute), it.amount, it.multiply) })
    }
    open class Reload : SimpleJsonResourceReloadListener(gson, "playerex/relic_weights") {
        override fun apply(data: Map<ResourceLocation, JsonElement>, manager: ResourceManager, profiler: ProfilerFiller) {
            val weights = linkedMapOf<String, RelicRollMath.Weights>()
            data.toSortedMap(compareBy(ResourceLocation::toString)).forEach { (resource, json) ->
                if (!json.isJsonObject) { PlayerEX.LOGGER.warn("Invalid relic weight file {}", resource); return@forEach }
                json.asJsonObject.entrySet().forEach { (key, value) ->
                    val id = ResourceLocation.tryParse(key)
                    if (id == null || !BuiltInRegistries.ATTRIBUTE.containsKey(id)) return@forEach
                    try {
                        val entry = gson.fromJson(value, RelicRollMath.Weights::class.java)
                        if (entry == null || !entry.valid()) PlayerEX.LOGGER.warn("Invalid relic weight {} in {}", key, resource)
                        else weights[key] = entry
                    } catch (error: com.google.gson.JsonParseException) {
                        PlayerEX.LOGGER.warn("Invalid relic weight {} in {}", key, resource, error)
                    }
                }
            }
            pool = RelicRollMath.Pool(weights)
            PlayerEX.LOGGER.info("Loaded {} relic attribute weights", weights.size)
        }
    }
}
