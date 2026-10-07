package com.bibireden.playerex.mixin;

import com.bibireden.playerex.item.ItemProgression;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackGameplayMixin {
    @Unique private static final EquipmentSlot[] PLAYEREX_EQUIPMENT_SLOTS = EquipmentSlot.values();

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void playerex$alreadyBroken(int amount, ServerLevel level, ServerPlayer player, Consumer<Item> onBreak, CallbackInfo ci) {
        if (ItemProgression.isBroken((ItemStack) (Object) this)) ci.cancel();
    }

    @Group(name = "playerexDurabilityBreak", min = 1, max = 1)
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), cancellable = true, require = 0)
    private void playerex$preserveBroken(int amount, ServerLevel level, ServerPlayer player, Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ItemProgression.preserveBroken(stack)) {
            ItemProgression.notifyBroken(stack, player);
            onBreak.accept(stack.getItem());
            ci.cancel();
        }
    }

    // Newer NeoForge 1.21.1 delegates durability to the LivingEntity overload.
    // The group requires exactly one actual break-site hook across both layouts.
    @Group(name = "playerexDurabilityBreak", min = 1, max = 1)
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = false), cancellable = true, require = 0, remap = false)
    private void playerex$preserveBrokenEntity(int amount, ServerLevel level, net.minecraft.world.entity.LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ItemProgression.preserveBroken(stack)) {
            ItemProgression.notifyBroken(stack, entity);
            onBreak.accept(stack.getItem());
            ci.cancel();
        }
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void playerex$alreadyBrokenEntity(int amount, ServerLevel level, net.minecraft.world.entity.LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
        if (ItemProgression.isBroken((ItemStack) (Object) this)) ci.cancel();
    }
    @Inject(method = "setDamageValue(I)V", at = @At("HEAD"))
    private void playerex$repair(int damage, CallbackInfo ci) { ItemProgression.repair((ItemStack) (Object) this, damage); }

    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At("HEAD"), cancellable = true)
    private void playerex$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ItemProgression.isBroken(stack)) cir.setReturnValue(InteractionResultHolder.fail(stack));
    }

    @Inject(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true)
    private void playerex$useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (ItemProgression.isBroken((ItemStack) (Object) this)) cir.setReturnValue(InteractionResult.FAIL);
    }

    @WrapMethod(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V")
    private void playerex$modifiers(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, Operation<Void> original) {
        ItemStack stack = (ItemStack) (Object) this;
        BiConsumer<Holder<Attribute>, AttributeModifier> filtered = (a, m) -> ItemProgression.filterModifier(stack, a, m, consumer);
        if (stack.has(com.bibireden.playerex.item.PlayerExItemComponents.RELIC) && stack.getItem() instanceof net.minecraft.world.item.ArmorItem) {
            java.util.List<kotlin.Pair<Holder<Attribute>, AttributeModifier>> base = new java.util.ArrayList<>();
            original.call(slot, (BiConsumer<Holder<Attribute>, AttributeModifier>) (a, m) -> base.add(new kotlin.Pair<>(a, m)));
            com.bibireden.playerex.item.RelicEquipment.mergeArmor(stack, slot, base, filtered);
        } else original.call(slot, filtered);
        ItemProgression.addModifiers(stack, slot, consumer);
    }

    @WrapMethod(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V")
    private void playerex$groupModifiers(EquipmentSlotGroup group, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, Operation<Void> original) {
        ItemStack stack = (ItemStack) (Object) this;
        BiConsumer<Holder<Attribute>, AttributeModifier> filtered = (a, m) -> ItemProgression.filterModifier(stack, a, m, consumer);
        if (stack.has(com.bibireden.playerex.item.PlayerExItemComponents.RELIC) && stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor && group == EquipmentSlotGroup.bySlot(armor.getEquipmentSlot())) {
            java.util.List<kotlin.Pair<Holder<Attribute>, AttributeModifier>> base = new java.util.ArrayList<>();
            original.call(group, (BiConsumer<Holder<Attribute>, AttributeModifier>) (a, m) -> base.add(new kotlin.Pair<>(a, m)));
            com.bibireden.playerex.item.RelicEquipment.mergeArmor(stack, armor.getEquipmentSlot(), base, filtered);
        } else original.call(group, filtered);
        // The tooltip path needs the same derived modifiers as equipment application.
        for (EquipmentSlot slot : PLAYEREX_EQUIPMENT_SLOTS) if (group == EquipmentSlotGroup.bySlot(slot)) ItemProgression.addModifiers(stack, slot, consumer);
    }
}
