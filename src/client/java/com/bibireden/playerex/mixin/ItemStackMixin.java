package com.bibireden.playerex.mixin;

import com.bibireden.data_attributes.api.item.ItemFields;
import com.bibireden.playerex.PlayerEX;
import com.bibireden.playerex.config.PlayerEXConfigModel;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Unique
    private double playerex$modifyValue(double val, @Nullable Player player, Attribute attribute, UUID uuid) {
        if (player == null || PlayerEX.CONFIG.getTooltip() == PlayerEXConfigModel.Tooltip.Default) return val;

        double valSubBase = val - player.getAttributeBaseValue(attribute);

        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return val;

        AttributeModifier modifier = instance.getModifier(uuid);
        double value = player.getAttributeValue(attribute) + valSubBase;

        if (modifier != null) value -= modifier.getAmount();

        return PlayerEX.CONFIG.getTooltip() == PlayerEXConfigModel.Tooltip.Vanilla ? valSubBase : value;
    }

    @Unique
    private String playerex$value(double e, Map.Entry<Attribute, AttributeModifier> entry, AttributeModifier modifier) {
        if (modifier.getOperation() != AttributeModifier.Operation.ADDITION) return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(e);
        return ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(e);
    }

    @ModifyVariable(method = "getTooltipLines", at = @At(value = "STORE", ordinal = 1), ordinal = 0)
    private double playerex$modifyAttackDamage(double original, Player player) {
        return playerex$modifyValue(original, player, Attributes.ATTACK_DAMAGE, ItemFields.attackDamageModifierID());
    }

    @ModifyVariable(method = "getTooltipLines", at = @At(value = "STORE", ordinal = 3), ordinal = 0)
    private double playerex$modifyAttackSpeed(double original, Player player) {
        return playerex$modifyValue(original, player, Attributes.ATTACK_SPEED, ItemFields.attackDamageModifierID());
    }

    @ModifyVariable(method = "getTooltipLines", at = @At(value = "STORE", ordinal = 1), ordinal = 0)
    private boolean playerex$flagAttackDamage(boolean original) {
        return PlayerEX.CONFIG.getTooltip() != PlayerEXConfigModel.Tooltip.Vanilla && original;
    }

    @ModifyVariable(method = "getTooltipLines", at = @At(value = "STORE", ordinal = 2), ordinal = 0)
    private boolean playerex$flagAttackSpeed(boolean original) {
        return PlayerEX.CONFIG.getTooltip() != PlayerEXConfigModel.Tooltip.Vanilla && original;
    }

    @ModifyVariable(method = "getTooltipLines", at = @At(value = "STORE", ordinal = 1), ordinal = 1)
    private double playerex$modifyAdditionAttributeKnockback(double original) { return original / 10.0; }

    // todo: not sure about the implementation(s) here...
    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 7, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void playerex$insertModifierEqualsTooltip(
        Player player, TooltipFlag context,
        CallbackInfoReturnable<List<Component>> info,
        List<Component> list,
        MutableComponent arg3,
        int arg4,
        EquipmentSlot[] arg5,
        int arg6,
        int arg7,
        EquipmentSlot arg8,
        Multimap<?, ?> arg9,
        Iterator<?> arg10,
        Map.Entry<Attribute, AttributeModifier> entry,
        AttributeModifier entityAttributeModifier,
        double arg13, double e
    ) {
        list.set(list.size() - 1, Component.literal(" ")
            .append(Component.translatable("attribute.modifier.equals." + entityAttributeModifier.getOperation().toValue(), playerex$value(e, entry, entityAttributeModifier), Component.translatable(entry.getKey().getDescriptionId())))
            .withStyle(ChatFormatting.DARK_GREEN)
        );
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 8, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void playerex$insertModifierPlusTooltip(
        Player player,
        TooltipFlag context,
        CallbackInfoReturnable<List<Component>> info,
        List<Component> list,
        MutableComponent arg3,
        int arg4,
        EquipmentSlot[] arg5,
        int arg6, int arg7, EquipmentSlot arg8, Multimap<?, ?> arg9, Iterator<?> arg10,
        Map.Entry<Attribute, AttributeModifier> entry, AttributeModifier entityAttributeModifier,
        double arg13, double e
    ) {
        list.set(
            list.size() - 1,
            Component.translatable("attribute.modifier.plus." + entityAttributeModifier.getOperation().toValue(),
            playerex$value(e, entry, entityAttributeModifier),
            Component.translatable(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.BLUE)
        );
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 9, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void playerex$insertModifierTakeTooltip(
        Player player, TooltipFlag context,
        CallbackInfoReturnable<List<Component>> info,
        List<Component> list,
        MutableComponent arg3,
        int arg4,
        EquipmentSlot[] arg5,
        int arg6,
        int arg7,
        EquipmentSlot arg8,
        Multimap<?, ?> arg9,
        Iterator<?> arg10,
        Map.Entry<Attribute, AttributeModifier> entry,
        AttributeModifier entityAttributeModifier,
        double arg13, double e
    ) {
        list.set(
            list.size() - 1,
            Component.translatable("attribute.modifier.take." + entityAttributeModifier.getOperation().toValue(),
            playerex$value(e, entry, entityAttributeModifier),
            Component.translatable(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.RED)
        );
    }
}
