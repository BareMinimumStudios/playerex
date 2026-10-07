package com.bibireden.playerex.client

import com.bibireden.playerex.PlayerEX
import com.bibireden.playerex.item.RelicEquipment
import com.google.gson.JsonParser
import net.minecraft.client.Minecraft
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity

/** Static RelicEx geometry baked into vanilla model parts; no animation library is needed. */
object RelicArmorModels {
    data class Models(val armor: HumanoidModel<LivingEntity>, val trim: HumanoidModel<LivingEntity>, val texture: ResourceLocation)
    private val cache = arrayOfNulls<Models>(7)
    @JvmStatic fun clear() { cache.fill(null) }
    @JvmStatic fun get(rarity: Int): Models {
        val index = rarity.coerceIn(0, 6)
        return cache[index] ?: bake(index).also { cache[index] = it }
    }
    private fun bake(rarity: Int): Models {
        val name = RelicEquipment.names[rarity]
        val geometry = Minecraft.getInstance().resourceManager.getResourceOrThrow(PlayerEX.id("geo/armors/$name.geo.json"))
            .openAsReader().use { JsonParser.parseReader(it).asJsonObject["minecraft:geometry"].asJsonArray[0].asJsonObject }
        return bakeGeometry(name, geometry)
    }
    private fun bakeGeometry(name: String, geometry: com.google.gson.JsonObject): Models {
        fun model(trim: Boolean): HumanoidModel<LivingEntity> {
            val mesh = MeshDefinition()
            val root = mesh.root
            val parts = listOf("head", "hat", "body", "right_arm", "left_arm", "right_leg", "left_leg")
                .associateWith { root.addOrReplaceChild(it, CubeListBuilder.create(), PartPose.ZERO) }
            val names = mapOf("armorHead" to "head", "armorBody" to "body", "armorRightArm" to "right_arm", "armorLeftArm" to "left_arm")
            for (boneElement in geometry["bones"].asJsonArray) {
                val bone = boneElement.asJsonObject
                val partName = names[bone["name"].asString] ?: continue
                val cubes = bone.getAsJsonArray("cubes") ?: continue
                val bonePivot = bone["pivot"].asJsonArray.map { it.asFloat }
                for ((index, element) in cubes.withIndex()) {
                    val cube = element.asJsonObject
                    val origin = cube["origin"].asJsonArray.map { it.asFloat }
                    val size = cube["size"].asJsonArray.map { it.asFloat }
                    val pivot = cube.getAsJsonArray("pivot")?.map { it.asFloat } ?: bonePivot
                    val rotation = cube.getAsJsonArray("rotation")?.map { Math.toRadians(it.asDouble).toFloat() } ?: listOf(0f, 0f, 0f)
                    val uv = if (trim) when (partName) { "head" -> listOf(0, 0); "body" -> listOf(16, 16); else -> listOf(40, 16) }
                        else cube["uv"].asJsonArray.map { it.asInt }
                    val builder = CubeListBuilder.create().texOffs(uv[0], uv[1]).mirror(cube["mirror"]?.asBoolean ?: false)
                        .addBox(origin[0] - pivot[0], -(origin[1] + size[1] - pivot[1]), origin[2] - pivot[2], size[0], size[1], size[2],
                            CubeDeformation((cube["inflate"]?.asFloat ?: 0f) + if (trim) 0.005f else 0f))
                    // AzureLib mirrors X in geometry, then X/Y in the armor pose.
                    // Conjugating its (-X, -Y, +Z) cube rotation leaves (+X, +Y, +Z).
                    parts.getValue(partName).addOrReplaceChild("cube_$index", builder,
                        PartPose.offsetAndRotation(pivot[0] - bonePivot[0], -(pivot[1] - bonePivot[1]), pivot[2] - bonePivot[2], rotation[0], rotation[1], rotation[2]))
                }
            }
            return HumanoidModel(LayerDefinition.create(mesh, 64, if (trim) 32 else 64).bakeRoot())
        }
        return Models(model(false), model(true), PlayerEX.id("textures/models/armor/$name.png"))
    }
}
