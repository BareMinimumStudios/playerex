package com.bibireden.playerex.factory

import com.bibireden.opc.api.OfflinePlayerCacheAPI
import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.api.PlayerEXCachedKeys
import com.bibireden.playerex.ext.level
import eu.pb4.placeholders.api.PlaceholderHandler
import eu.pb4.placeholders.api.PlaceholderResult
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import kotlin.math.max

object PlaceholderFactory {
    val STORE: MutableMap<ResourceLocation, PlaceholderHandler> = mutableMapOf();

    private fun nameLevelPair(server: MinecraftServer, namesIn: Collection<String>, indexIn: Int): Pair<String, Int>
    {
        val cache = OfflinePlayerCacheAPI.getCache(server)

        val names: ArrayList<Pair<String, Int>> = ArrayList(namesIn.size);

        var i = 0

        for (name: String in namesIn) {
            cache.getEntry(PlayerEXCachedKeys.Level::class.java, name).ifPresent {
                names[i] = Pair(name, it.level)
                i++
            }
        }

        names.sortWith(Comparator.comparing { (_, level) -> level })

        val j = Mth.clamp(indexIn, 1, names.size)

        return names[names.size - j]
    }

    private fun top(stringFunction: (Pair<String, Int>) -> String): PlaceholderHandler
    {
        return PlaceholderHandler { ctx, arg ->
            val server = ctx.server()
            val cache = OfflinePlayerCacheAPI.getCache(server)

            var index: Int = 1

            val names: Collection<String> = cache.usernames

            if (arg !== null)
            {
                try {
                    val i: Int = arg.toInt()
                    index = max(1, i)
                } catch (e: NumberFormatException)
                {
                    return@PlaceholderHandler PlaceholderResult.invalid("Invalid argument!")
                }
            }

            if (index > names.size) return@PlaceholderHandler PlaceholderResult.value("")
            val pair: Pair<String, Int> = this.nameLevelPair(server, names, index)
            return@PlaceholderHandler PlaceholderResult.value(stringFunction.invoke(pair))
        }
    }

    init {
        val levelId = ResourceLocation.tryBuild(PlayerEX.MOD_ID, "level")
        val nameTopId = ResourceLocation.tryBuild(PlayerEX.MOD_ID, "name_top")
        val levelTopId = ResourceLocation.tryBuild(PlayerEX.MOD_ID, "level_top")

        if (levelId != null) {
            STORE[levelId] = PlaceholderHandler { ctx, _ ->
                val player: ServerPlayer? = ctx?.player

                player ?: return@PlaceholderHandler PlaceholderResult.invalid("No player!");

                return@PlaceholderHandler PlaceholderResult.value(player.level.toString())
            }
        }

        if (nameTopId != null) {
            STORE[nameTopId] = top { (name, _) -> name}
        }

        if (levelTopId != null) {
            STORE[levelTopId] = top { (_, level) -> level.toString()}
        }


    }
}