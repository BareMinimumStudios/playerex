package com.bibireden.playerex.mixin;

import com.bibireden.playerex.item.ItemProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerGameplayMixin {
    @Inject(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void playerex$brokenAttack(Entity target, CallbackInfo ci) {
        if (ItemProgression.isBroken(((Player) (Object) this).getMainHandItem())) ci.cancel();
    }

    @Inject(method = "interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true)
    private void playerex$brokenInteract(Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (ItemProgression.isBroken(((Player) (Object) this).getItemInHand(hand))) cir.setReturnValue(InteractionResult.FAIL);
    }

    @Inject(method = "blockActionRestricted(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/GameType;)Z", at = @At("HEAD"), cancellable = true)
    private void playerex$brokenMining(Level level, BlockPos pos, GameType type, CallbackInfoReturnable<Boolean> cir) {
        if (ItemProgression.isBroken(((Player) (Object) this).getMainHandItem())) cir.setReturnValue(true);
    }
}
