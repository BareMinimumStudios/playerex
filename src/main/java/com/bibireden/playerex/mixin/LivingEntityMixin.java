package com.bibireden.playerex.mixin;

import com.bibireden.playerex.api.event.LivingEntityEvents;
import com.bibireden.playerex.util.PlayerEXUtil;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    final private int TICKS_UNTIL_RESET = 20;

    @Unique
    private int playerex_ticks;

    @Inject(method = "setCurrentHand(Lnet/minecraft/util/Hand;)V", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void preventAttack(Hand hand, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (PlayerEXUtil.isBroken(entity.getStackInHand(hand))) {
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
        if (!cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void playerex$tick(CallbackInfo ci) {
        if (this.playerex_ticks < this.TICKS_UNTIL_RESET) {
            this.playerex_ticks++;
        }
        else {
            LivingEntityEvents.ON_TICK.invoker().onTick((LivingEntity) (Object) this);
        }
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float playerex$damage(float original, DamageSource source) {
        return LivingEntityEvents.ON_DAMAGE.invoker().onDamage((LivingEntity) (Object) this, source, original);
    }

    @ModifyReturnValue(method = "damage", at = @At("RETURN"))
    private boolean playerex$damage(boolean original, DamageSource source, float damage) {
        boolean cancelled = LivingEntityEvents.SHOULD_DAMAGE.invoker().shouldDamage((LivingEntity) (Object) this, source, damage);
        return cancelled && original;
    }
}
