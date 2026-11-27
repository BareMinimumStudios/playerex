package com.bibireden.playerex.mixin;

import com.bibireden.playerex.item.ItemProgression;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class NeoForgeDurabilityMixin {
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
    private void playerex$preserveBrokenEntity(int amount, ServerLevel level, LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ItemProgression.preserveBroken(stack)) {
            ItemProgression.notifyBroken(stack, entity);
            onBreak.accept(stack.getItem());
            ci.cancel();
        }
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void playerex$alreadyBrokenEntity(int amount, ServerLevel level, LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
        if (ItemProgression.isBroken((ItemStack) (Object) this)) ci.cancel();
    }
}
