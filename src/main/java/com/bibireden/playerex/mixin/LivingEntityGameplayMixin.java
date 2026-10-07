package com.bibireden.playerex.mixin;

import com.bibireden.playerex.progression.PlayerGameplay;
import com.bibireden.playerex.item.ItemProgression;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGameplayMixin {
    @Inject(method = "startUsingItem(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
    private void playerex$brokenStartUsing(net.minecraft.world.InteractionHand hand, CallbackInfo ci) {
        if (ItemProgression.isBroken(((LivingEntity) (Object) this).getItemInHand(hand))) ci.cancel();
    }

    @ModifyReturnValue(method = "getExperienceReward(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)I", at = @At("RETURN"))
    private int playerex$experience(int original, ServerLevel level, Entity killer) {
        return PlayerGameplay.experienceReward(original, killer);
    }

    @ModifyVariable(method = "heal(F)V", at = @At("HEAD"), argsOnly = true)
    private float playerex$healing(float amount) {
        return PlayerGameplay.amplifyHealing((LivingEntity) (Object) this, amount);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void playerex$regeneration(CallbackInfo ci) {
        PlayerGameplay.regenerate((LivingEntity) (Object) this);
    }

    @ModifyReturnValue(method = "getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", at = @At("RETURN"))
    private float playerex$armorReduction(float amount) {
        return ItemProgression.armorReduction((LivingEntity) (Object) this, amount);
    }

    @WrapOperation(method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;killedEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean playerex$itemExperience(Entity attacker, ServerLevel level, LivingEntity victim, Operation<Boolean> original) {
        boolean result = original.call(attacker, level, victim);
        if (attacker instanceof ServerPlayer player) ItemProgression.onKilled(victim, player);
        return result;
    }

    @Inject(method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V", at = @At("TAIL"))
    private void playerex$relicDrop(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        com.bibireden.playerex.item.RelicLoot.mob((LivingEntity) (Object) this, source, causedByPlayer);
    }

    @WrapMethod(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    private boolean playerex$damage(DamageSource source, float amount, Operation<Boolean> original) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide()) return original.call(source, amount);
        if (amount > 0.0f && PlayerGameplay.evade(entity, source)) return false;
        float modified = com.bibireden.playerex.registry.DamageModificationRegistry.INSTANCE.apply(entity, source, PlayerGameplay.resist(entity, source, amount));
        if (amount > 0.0f && modified <= 0.0f) return false;
        float healthBefore = entity.getHealth();
        boolean accepted = original.call(source, modified);
        PlayerGameplay.stealLife(entity, source, healthBefore, accepted);
        return accepted;
    }
}
