package com.bibireden.playerex.mixin;

import com.bibireden.playerex.api.event.LivingEntityEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    final private int TICKS_UNTIL_RESET = 20;

    @Unique
    private int playerex_ticks;

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

    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float playerex$damage(float original, DamageSource source) {
        return LivingEntityEvents.ON_DAMAGE.invoker().onDamage((LivingEntity) (Object) this, source, original);
    }

    @ModifyReturnValue(method = "hurt", at = @At("RETURN"))
    private boolean playerex$damage(boolean original, DamageSource source, float damage) {
        boolean cancelled = LivingEntityEvents.SHOULD_DAMAGE.invoker().shouldDamage((LivingEntity) (Object) this, source, damage);
        return cancelled && original;
    }
}
