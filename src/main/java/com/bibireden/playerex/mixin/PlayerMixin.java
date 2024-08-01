package com.bibireden.playerex.mixin;

import com.bibireden.playerex.api.event.PlayerEntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyVariable(method = "attack", at = @At("STORE"), name = "bl3", ordinal = 2)
    private boolean playerex_attack(boolean bl3, Entity target) {
        return PlayerEntityEvents.SHOULD_CRITICAL.invoker().shouldCritical((Player)(Object) this, target, bl3);
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 2), name = "f", ordinal = 0)
    private float playerex_attack(float f, Entity target) {
        return PlayerEntityEvents.ON_CRITICAL.invoker().onCriticalDamage((Player) (Object) this, target, f);
    }
}
