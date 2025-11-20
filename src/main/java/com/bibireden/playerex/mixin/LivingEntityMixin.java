package com.bibireden.playerex.mixin;

import com.bibireden.playerex.PlayerEX;
import com.bibireden.playerex.api.event.LivingEntityEvents;
import com.bibireden.playerex.util.PlayerEXUtil;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private int playerex_ticks;

    @Unique
    final int ON_EVERY_SECOND_TICKS = 20;

    @Inject(method = "startUsingItem(Lnet/minecraft/world/InteractionHand;)V", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void preventAttack(InteractionHand hand, CallbackInfo ci) {
        if (!PlayerEX.CONFIG.getFeatureSettings().getItemBreakingEnabled()) return;

        LivingEntity entity = (LivingEntity)(Object)this;
        if (PlayerEXUtil.isBroken(entity.getItemInHand(hand))) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float playerex$heal(float original) {
        return LivingEntityEvents.ON_HEAL.invoker().onHeal((LivingEntity) (Object) this, original);
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void playerex$heal(float original, CallbackInfo ci) {
        final boolean cancelled = LivingEntityEvents.SHOULD_HEAL.invoker().shouldHeal((LivingEntity) (Object) this, original);
        if (!cancelled) ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void playerex$tick(CallbackInfo ci) {
        if (this.playerex_ticks < ON_EVERY_SECOND_TICKS) {
            this.playerex_ticks++;
        }
        else {
            LivingEntityEvents.ON_EVERY_SECOND.invoker().onEverySecond((LivingEntity) (Object) this);
            this.playerex_ticks = 0;
        }
        LivingEntityEvents.ON_TICK.invoker().onTick((LivingEntity) (Object) this);
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float playerex$damage(float original, DamageSource source) {
        return LivingEntityEvents.ON_DAMAGE.invoker().onDamage((LivingEntity) (Object) this, source, original);
    }

    @ModifyReturnValue(method = "hurt", at = @At("RETURN"))
    private boolean playerex$damage(boolean original, DamageSource source, float damage) {
        return LivingEntityEvents.SHOULD_DAMAGE.invoker().shouldDamage((LivingEntity) (Object) this, source, damage);
    }

    @ModifyReturnValue(
            method = "getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F",
            at = @At("RETURN")
    )
    public float applyDamageReduction(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        var total = 0d;
        for (ItemStack slot : self.getArmorSlots()) {
            total += Math.min((slot.hasTag() ? slot.getTag().getInt("Level") : 0) * PlayerEX.CONFIG.getArmorLevelingSettings().getReductionPerLevel(), PlayerEX.CONFIG.getArmorLevelingSettings().getMaxReduction());
        }
        // 400 because 4 slots then percentage to decimal
        total /= 400;
        return (float) (original * (1 - total));
    }
}
