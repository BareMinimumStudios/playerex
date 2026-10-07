package com.bibireden.playerex.command

import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.api.attribute.TradeSkillAttributes
import com.bibireden.playerex.item.ItemProgression
import com.bibireden.playerex.item.PlayerExItemComponents
import com.bibireden.playerex.progression.PlayerProgression
import com.bibireden.playerex.state.PlayerStateService
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.ai.attributes.RangedAttribute

private typealias Context = CommandContext<CommandSourceStack>

object PlayerExCommands {
    private fun player() = Commands.argument("player", EntityArgument.player())
    private fun amount() = Commands.argument("amount", IntegerArgumentType.integer())
    private fun id() = Commands.argument("id", ResourceLocationArgument.id()).suggests { _, builder ->
        SharedSuggestionProvider.suggestResource(PlayerEXAttributes.ALLOCATION_ATTRIBUTE_IDS + TradeSkillAttributes.IDS, builder)
    }

    @JvmStatic
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("playerex").requires { it.hasPermission(2) }
            .then(Commands.literal("level")
                .then(Commands.literal("get").then(player().executes {
                    val target = EntityArgument.getPlayer(it, "player")
                    success(it, Component.translatable("playerex.command.level_get", target.name, PlayerStateService.get(target).level)
                        .append("/${PlayerEXAttributes.get(PlayerEXAttributes.LEVEL_ID)?.maxValue?.toInt() ?: 0}"))
                }))
                .then(Commands.literal("add").then(player().executes { level(it, 1) }
                    .then(amount().executes { level(it, IntegerArgumentType.getInteger(it, "amount")) }))))
            .then(Commands.literal("reset")
                .then(player().executes { reset(it, 0, false) }.then(Commands.argument("retain", IntegerArgumentType.integer(0, 100))
                    .executes { reset(it, IntegerArgumentType.getInteger(it, "retain"), false) }))
                .then(Commands.literal("@all").executes { reset(it, 0, true) }.then(amount()
                    .executes { reset(it, IntegerArgumentType.getInteger(it, "amount"), true) })))
            .then(Commands.literal("skill").then(id()
                .then(Commands.literal("get").then(player().executes { skill(it, 0, true) }))
                .then(Commands.literal("add").then(player().executes { skill(it, 1, false) }
                    .then(amount().executes { skill(it, IntegerArgumentType.getInteger(it, "amount"), false) })))))
            .then(Commands.literal("refund")
                .then(Commands.literal("get").then(player().executes {
                    val target = EntityArgument.getPlayer(it, "player")
                    success(it, Component.translatable("playerex.command.refund.get", target.name, PlayerStateService.get(target).refundablePoints))
                }))
                .then(Commands.literal("add").then(player().executes { refundPoints(it, 1) }
                    .then(amount().executes { refundPoints(it, IntegerArgumentType.getInteger(it, "amount")) })))
                .then(Commands.literal("skill").then(id().then(player().then(amount().executes { refund(it) })))))
            .then(itemCommands("armor", true))
            .then(itemCommands("weapon", false)))
    }

    private fun level(context: Context, amount: Int): Int {
        val target = EntityArgument.getPlayer(context, "player")
        val maximum = PlayerEXAttributes.get(PlayerEXAttributes.LEVEL_ID)?.maxValue?.toInt() ?: return rejected(context)
        val actual = amount.coerceIn(0, (maximum - PlayerStateService.get(target).level).coerceAtLeast(0))
        if (!PlayerProgression.grantLevel(target, actual)) return rejected(context)
        success(context, Component.translatable("playerex.command.level_up", actual, target.name))
        return success(context, updated(PlayerStateService.get(target).level, maximum))
    }

    private fun skill(context: Context, amount: Int, query: Boolean): Int {
        val target = EntityArgument.getPlayer(context, "player")
        val attributeId = ResourceLocationArgument.getId(context, "id")
        val attribute = BuiltInRegistries.ATTRIBUTE.get(attributeId) ?: return rejected(context)
        val instance = target.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute)) ?: return rejected(context)
        val name = Component.translatable(attribute.descriptionId)
        if (query) return success(context, Component.translatable("playerex.command.skill_get", name, instance.value.toInt(), target.name))
        val maximum = (attribute as? RangedAttribute)?.maxValue?.toInt() ?: return rejected(context)
        val actual = amount.coerceIn(0, (maximum - instance.value.toInt()).coerceAtLeast(0))
        if (!PlayerProgression.grantSkill(target, attributeId, actual)) {
            context.source.sendFailure(Component.translatable("playerex.command.max_error", name, target.name))
            return -1
        }
        success(context, Component.translatable("playerex.command.skill_up", actual, name, target.name))
        return success(context, updated(instance.value.toInt(), maximum))
    }

    private fun refundPoints(context: Context, amount: Int): Int {
        val target = EntityArgument.getPlayer(context, "player")
        PlayerProgression.adjustRefundPoints(target, amount)
        return success(context, Component.translatable("playerex.command.refund.add", amount, target.name))
    }

    private fun refund(context: Context): Int {
        val target = EntityArgument.getPlayer(context, "player")
        val attributeId = ResourceLocationArgument.getId(context, "id")
        val attribute = BuiltInRegistries.ATTRIBUTE.get(attributeId) ?: return rejected(context)
        val instance = target.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute)) ?: return rejected(context)
        val actual = IntegerArgumentType.getInteger(context, "amount").coerceIn(0, instance.value.toInt().coerceAtLeast(0))
        if (!PlayerProgression.refundAdmin(target, attributeId, actual)) return rejected(context)
        success(context, Component.translatable("playerex.command.refunded", actual, Component.translatable(attribute.descriptionId), target.name))
        return success(context, updated(instance.value.toInt(), (attribute as? RangedAttribute)?.maxValue?.toInt() ?: 0))
    }

    private fun reset(context: Context, retain: Int, all: Boolean): Int {
        val retained = retain.coerceIn(0, 100)
        val targets = if (all) context.source.server.playerList.players else listOf(EntityArgument.getPlayer(context, "player"))
        targets.forEach { PlayerProgression.reset(it, retained) }
        return success(context, Component.translatable("playerex.command.reset", if (all) Component.literal("(*)") else targets.single().name)
            .also { if (retained > 0) it.append(" [$retained%]") })
    }

    private fun itemCommands(kind: String, armor: Boolean) = Commands.literal(kind).then(Commands.literal("level")
        .then(Commands.literal("reset").executes { itemLevel(it, armor, true, 0) })
        .then(Commands.literal("set").then(Commands.argument("amount", IntegerArgumentType.integer(0, 10000))
            .executes { itemLevel(it, armor, false, IntegerArgumentType.getInteger(it, "amount")) })))

    private fun itemLevel(context: Context, armor: Boolean, reset: Boolean, level: Int): Int {
        val stack = context.source.playerOrException.mainHandItem
        val key = "playerex.command.${if (reset) "reset" else "set"}_${if (armor) "armor" else "weapon"}"
        if (!(if (armor) ItemProgression.isArmor(stack) else ItemProgression.isWeapon(stack))) {
            context.source.sendFailure(Component.translatable("$key.failure"))
            return -1
        }
        stack.set(PlayerExItemComponents.PROGRESSION, ItemProgression.state(stack).copy(level = level, experience = 0))
        context.source.playerOrException.inventory.setChanged()
        return success(context, Component.translatable("$key.success", level), true)
    }

    private fun updated(value: Int, maximum: Int) = Component.translatable("playerex.command.updated_result", value).append("/$maximum")

    private fun success(context: Context, message: Component, broadcast: Boolean = false): Int {
        context.source.sendSuccess({ message }, broadcast)
        return 1
    }

    private fun rejected(context: Context): Int {
        context.source.sendFailure(Component.translatable("playerex.command.rejected"))
        return -1
    }
}
