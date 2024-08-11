package com.bibireden.playerex

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.attribute.EntityAttributeSupplier
import com.bibireden.data_attributes.api.attribute.IEntityAttribute
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.api.attribute.TradeSkillAttributes
import com.bibireden.playerex.components.PlayerEXComponents
import com.bibireden.playerex.ext.dataComponent
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.CommandSelection
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.player.Player

private typealias Context = CommandContext<CommandSourceStack>

object PlayerEXCommands {
    private val MODIFIABLE_ATTRIBUTES_SUGGESTIONS = SuggestionProvider<CommandSourceStack> { _, builder ->
        SharedSuggestionProvider.suggestResource(PlayerEXAttributes.PRIMARY_ATTRIBUTE_IDS, builder)
        SharedSuggestionProvider.suggestResource(TradeSkillAttributes.IDS, builder)
    }

    private val playerArgument: RequiredArgumentBuilder<CommandSourceStack, EntitySelector>
        get() = Commands.argument("player", EntityArgument.player())

    private val amountArgument: RequiredArgumentBuilder<CommandSourceStack, Int>
        get() = Commands.argument("amount", IntegerArgumentType.integer())

    private val identifierArgument: RequiredArgumentBuilder<CommandSourceStack, ResourceLocation>
        get() = Commands.argument("id", ResourceLocationArgument.id())

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, ctx: CommandBuildContext, selection: CommandSelection) {
        dispatcher.register(Commands.literal("playerex")
            .requires(::isOp)
            .then(Commands.literal("level")
                .then(Commands.literal("get").then(playerArgument.executes(::executeLevelGetCommand)))
                .then(Commands.literal("add").then(
                    playerArgument.executes(::executeLevelUpCommand)
                        .then(amountArgument.executes { executeLevelUpCommand(it, IntegerArgumentType.getInteger(it, "amount")) })
                    )
                )
            )
            .then(Commands.literal("reset")
                .then(playerArgument.executes(::executeResetCommand)
                    .then(Commands.argument("retain", IntegerArgumentType.integer(0, 100)).executes { executeResetCommand(it, IntegerArgumentType.getInteger(it, "retain")) })
                )
                .then(Commands.literal("@all").executes(::executeResetAllCommand)
                    .then(amountArgument.executes { executeResetAllCommand(it, IntegerArgumentType.getInteger(it, "amount")) })
                )
            )
            .then(Commands.literal("skill")
                .then(identifierArgument.suggests(MODIFIABLE_ATTRIBUTES_SUGGESTIONS)
                    .then(Commands.literal("get").then(playerArgument.executes(::executeSkillGetCommand)))
                    .then(Commands.literal("add")
                        .then(playerArgument
                            .executes(::executeSkillUpCommand)
                            .then(amountArgument.executes { executeSkillUpCommand(it, IntegerArgumentType.getInteger(it, "amount")) })
                        )
                    )
                )
            ).then(Commands.literal("refund")
                .then(Commands.literal("get").then(playerArgument.executes(::executeRefundGetCommand)))
                .then(
                    Commands.literal("add").then(
                        playerArgument.executes(::executeRefundAddCommand).then(
                            amountArgument.executes { executeRefundAddCommand(it, IntegerArgumentType.getInteger(it, "amount")) }
                        )
                    )
                )
                .then(
                    Commands.literal("skill").then(
                        identifierArgument.suggests(MODIFIABLE_ATTRIBUTES_SUGGESTIONS)
                            .then(playerArgument.then(
                                amountArgument.executes { executeRefundAttributeCommand(it, IntegerArgumentType.getInteger(it, "amount")) }
                            )
                        )
                    )
                )
            )
        )
    }

    private fun isOp(source: CommandSourceStack) = source.hasPermission(2)

    private fun executeLevelGetCommand(ctx: Context): Int {
        val player = EntityArgument.getPlayer(ctx, "player")
        return DataAttributesAPI.getValue(PlayerEXAttributes.LEVEL, player).map {
            ctx.source.sendSuccess({ Component.translatable("playerex.command.level_get", player.name, it.toInt()).append(Component.nullToEmpty("/${PlayerEXAttributes.LEVEL.maxValue.toInt()}")) }, false)
            1
        }.orElse(-1)
    }
    
    private fun executeSkillGetCommand(ctx: Context): Int {
        val player = EntityArgument.getPlayer(ctx, "player")
        val component = PlayerEXComponents.PLAYER_DATA.get(player)
        val supplier = EntityAttributeSupplier(ResourceLocationArgument.getId(ctx, "id"))

        return DataAttributesAPI.getValue(supplier, player).map { value ->
            ctx.source.sendSuccess({ Component.translatable("playerex.command.skill_get", Component.translatable(supplier.get().get().descriptionId), value.toInt(), player.name) }, false)
            1
        }.orElse(-1)
    }

    private fun executeRefundGetCommand(ctx: Context): Int {
        val player = EntityArgument.getPlayer(ctx, "player")
        ctx.source.sendSuccess({ Component.translatable("playerex.command.refund.get", player.name, player.dataComponent.refundablePoints) }, false)
        return 1
    }

    private fun executeRefundAttributeCommand(ctx: Context, amount: Int = 1): Int {
        val player = EntityArgument.getPlayer(ctx, "player")

        val supplier = EntityAttributeSupplier(ResourceLocationArgument.getId(ctx, "id"))

        return DataAttributesAPI.getValue(supplier, player).map {
            val attribute = supplier.get().get()
            val computed = Mth.clamp(amount, 0, it.toInt())

            if (player.dataComponent.refund(attribute, computed)) {
                ctx.source.sendSuccess({ Component.translatable("playerex.command.refunded", amount, Component.translatable(attribute.descriptionId), player.name) }, false)
                ctx.source.sendSuccess(updatedValueText(attribute, it - amount), false)
                1
            }
            else {
                -1
            }
        }.orElse(-1)
    }

    private fun executeRefundAddCommand(ctx: Context, amount: Int = 1): Int {
        val player = EntityArgument.getPlayer(ctx, "player")

        player.dataComponent.addRefundablePoints(amount)

        ctx.source.sendSuccess({ Component.translatable("playerex.command.refund.add", amount, player.name) }, false)

        return 1
    }

    private fun executeSkillUpCommand(ctx: Context, amount: Int = 1): Int {
        val player = EntityArgument.getPlayer(ctx, "player")
        val supplier = EntityAttributeSupplier(ResourceLocationArgument.getId(ctx, "id"))

        return DataAttributesAPI.getValue(supplier, player).map {
            val attribute = supplier.get().get()
            val computed = Mth.clamp(amount, 0, (attribute as IEntityAttribute).`data_attributes$max`().toInt() - it.toInt())

            if (player.dataComponent.skillUp(attribute, computed, true)) {
                ctx.source.sendSuccess({ Component.translatable("playerex.command.skill_up", computed, Component.translatable(attribute.descriptionId), player.name) }, false)
                ctx.source.sendSuccess(updatedValueText(attribute, it + computed), false)
                1
            }
            else {
                ctx.source.sendSuccess(maxErrorMessage(player, attribute), false)
                -1
            }
        }.orElse(-1)
    }

    private fun executeLevelUpCommand(ctx: Context, amount: Int = 1): Int {
        val player = EntityArgument.getPlayer(ctx, "player")

        return DataAttributesAPI.getValue(PlayerEXAttributes.LEVEL, player).map { value ->
            val attribute = PlayerEXAttributes.LEVEL
            val computed = Mth.clamp(amount, 0, (attribute as IEntityAttribute).`data_attributes$max`().toInt() - value.toInt())

            if (!player.dataComponent.levelUp(computed, true)) {
                // todo: err message, for now just -1
                return@map -1
            }

            ctx.source.sendSuccess({ Component.translatable("playerex.command.level_up", computed, player.name) }, false)
            ctx.source.sendSuccess(updatedValueText(attribute, value + computed), false)

            return@map 1
        }.orElse(-1)
    }

    private fun executeResetCommand(ctx: Context, retained: Int = 0): Int {
        val player = EntityArgument.getPlayer(ctx, "player")

        PlayerEXComponents.PLAYER_DATA.get(player).reset(retained)

        ctx.source.sendSuccess(
            {
                Component.translatable("playerex.command.reset", player.name).also { if (retained > 0) it.append(" [${retained}%]") }
            },
            false
        )

        return 1
    }

    private fun executeResetAllCommand(ctx: Context, retained: Int = 0): Int {
        PlayerLookup.all(ctx.source.server).forEach { PlayerEXComponents.PLAYER_DATA.get(it).reset(retained) }

        ctx.source.sendSuccess(
            {
                Component.translatable("playerex.command.reset", "(*)")
                    .also { if (retained > 0) it.append(", ${retained}%") }
            },
            false
        )

        return 1
    }

    private fun maxErrorMessage(player: Player, attribute: Attribute): () -> MutableComponent = {
        Component.translatable("playerex.command.max_error", Component.translatable(attribute.descriptionId), player.name)
    }

    private fun updatedValueText(attribute: Attribute, value: Double): () -> MutableComponent = {
        Component.translatable("playerex.command.updated_result", value.toInt()).append(Component.nullToEmpty("/${(attribute as IEntityAttribute).`data_attributes$max`().toInt()}"))
    }
}