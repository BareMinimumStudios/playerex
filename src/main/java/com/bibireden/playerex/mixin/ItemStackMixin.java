package com.bibireden.playerex.mixin;

import com.bibireden.data_attributes.api.item.ItemFields;
import com.bibireden.playerex.PlayerEX;
import com.bibireden.playerex.api.PlayerEXTags;
import com.bibireden.playerex.config.PlayerEXConfigModel;
import com.bibireden.playerex.util.PlayerEXUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Shadow
    public abstract boolean hurt(int amount, RandomSource random, @Nullable ServerPlayer serverPlayer);

    @Shadow
    public abstract CompoundTag getOrCreateTag();

    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At(value = "HEAD"), cancellable = true)
    public void preventUse(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!PlayerEX.CONFIG.getItemBreakingEnabled()) return;

        ItemStack stack = (ItemStack) (Object) this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }

    @Inject(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "HEAD"), cancellable = true)
    public void preventUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!PlayerEX.CONFIG.getItemBreakingEnabled()) return;

        ItemStack stack = (ItemStack) (Object) this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", at = @At(value = "HEAD"), cancellable = true)
    public void preventDamage(int amount, RandomSource random, ServerPlayer user, CallbackInfoReturnable<Boolean> cir) {
        if (!PlayerEX.CONFIG.getItemBreakingEnabled()) return;

        ItemStack stack = (ItemStack) (Object) this;
        if (PlayerEXUtil.isBroken(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"), cancellable = true)
    public <T extends LivingEntity> void preventBreak(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        if (!PlayerEX.CONFIG.getItemBreakingEnabled()) return;

        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItemHolder().is(PlayerEXTags.UNBREAKABLE_ITEMS)) {
            if (!PlayerEXUtil.isBroken(stack)) {
                CompoundTag tag = stack.getTag();
                tag.putBoolean("broken", true);
                stack.setTag(tag);
            }
            ci.cancel();
        }
    }

    @Inject(method = "setDamageValue(I)V", at = @At(value = "HEAD"))
    public void removeBrokenOnRepair(int damage, CallbackInfo ci) {
        if (!PlayerEX.CONFIG.getItemBreakingEnabled()) return;

        ItemStack stack = (ItemStack) (Object) this;
        if (PlayerEXUtil.isBroken(stack) && damage < stack.getDamageValue()) {
            CompoundTag tag = stack.getTag();
            tag.putBoolean("broken", false);
            stack.setTag(tag);
        }
    }

    @Inject(method = "getAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;", at = @At(value = "RETURN"), cancellable = true)
    public void modifyAttributeModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        if (!PlayerEX.CONFIG.getItemBreakingEnabled()) return;

        ItemStack stack = (ItemStack) (Object) this;
        HashMultimap<Attribute, AttributeModifier> hashmap = HashMultimap.create(cir.getReturnValue());
        if (PlayerEX.CONFIG.getWeaponLevelingSettings().getEnabled() && PlayerEXUtil.isWeapon(stack)) {
            PlayerEXUtil.addToModifier(hashmap, Attributes.ATTACK_DAMAGE, getLevel() * PlayerEX.CONFIG.getWeaponLevelingSettings().getDamagePerLevel());
        }
        if (PlayerEX.CONFIG.getArmorLevelingSettings().getEnabled() && PlayerEXUtil.isArmor(stack)) {
            PlayerEXUtil.addToModifier(hashmap, Attributes.ARMOR, getLevel() * PlayerEX.CONFIG.getArmorLevelingSettings().getArmorPerLevel());
        }
        if (PlayerEXUtil.isBroken(stack)) {
            PlayerEXUtil.removeModifier(hashmap, Attributes.ARMOR);
            PlayerEXUtil.removeModifier(hashmap, Attributes.ARMOR_TOUGHNESS);
            PlayerEXUtil.removeModifier(hashmap, Attributes.KNOCKBACK_RESISTANCE);
            PlayerEXUtil.removeModifier(hashmap, Attributes.ATTACK_DAMAGE);
            PlayerEXUtil.removeModifier(hashmap, Attributes.ATTACK_SPEED);
        }
        cir.setReturnValue(hashmap);
    }

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
    private double playerex$modifyAdditionAttributeKnockback(double original) {
        return original / 10.0;
    }

    // todo: not sure about the implementation(s) here...
    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 7, shift = At.Shift.AFTER))
    private void playerex$insertModifierEqualsTooltip(
            Player player, TooltipFlag context,
            CallbackInfoReturnable<List<Component>> info,
            @Local List<Component> list,
            @Local Map.Entry<Attribute, AttributeModifier> entry,
            @Local AttributeModifier modifier,
            @Local(ordinal = 1) double e
    ) {
        list.set(list.size() - 1, Component.literal(" ")
                .append(Component.translatable("attribute.modifier.equals." + modifier.getOperation().toValue(), playerex$value(e, entry, modifier), Component.translatable(entry.getKey().getDescriptionId())))
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

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 9, shift = At.Shift.AFTER))
    private void playerex$insertModifierTakeTooltip(
            Player player, TooltipFlag context,
            CallbackInfoReturnable<List<Component>> info,
            @Local List<Component> list,
            @Local Map.Entry<Attribute, AttributeModifier> entry,
            @Local AttributeModifier modifier,
            @Local(ordinal = 1) double e
    ) {
        list.set(
                list.size() - 1,
                Component.translatable("attribute.modifier.take." + modifier.getOperation().toValue(),
                        playerex$value(e, entry, modifier),
                        Component.translatable(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.RED)
        );
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.AFTER))
    private void playerex$insertBrokenTooltip(
            Player player, TooltipFlag context,
            CallbackInfoReturnable<List<Component>> info,
            @Local List<Component> list
    ) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (PlayerEXUtil.isBroken(itemStack)) {
            list.add(
                    Component.translatable("playerex.broken")
                            .withStyle(ChatFormatting.RED)
                            .withStyle(ChatFormatting.BOLD)
            );
        }
    }

    @Unique
    private int getLevel() {
        ItemStack itemStack = (ItemStack) (Object) this;
        return itemStack.getOrCreateTag().getInt("Level");
    }

    @Unique
    private int getXp() {
        ItemStack itemStack = (ItemStack) (Object) this;
        return itemStack.getOrCreateTag().getInt("Experience");
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.AFTER))
    private void playerex$insertLevelTooltip(
            Player player, TooltipFlag context,
            CallbackInfoReturnable<List<Component>> info,
            @Local List<Component> list
    ) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (PlayerEXUtil.isLevelable(itemStack)) {
            list.add(Component.translatable("playerex.item.level", getLevel(), PlayerEXUtil.getMaxLevel(itemStack)));
            list.add(Component.translatable("playerex.item.experience", getXp(), PlayerEXUtil.getRequiredXpForNextLevel(itemStack)));
        }
    }
}
