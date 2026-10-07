package com.bibireden.playerex.mixin;

import com.bibireden.playerex.progression.PlayerGameplay;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(Player.class)
public abstract class FabricPlayerCombatMixin {
    // Verified 1.21.1 attack bytecode: boolean local #2 is the crit flag, local #0 is charge eligibility.
    @ModifyVariable(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At("STORE"), ordinal = 2)
    private boolean playerex$critical(boolean original, Entity target, @Local(ordinal = 0) boolean charged) {
        return PlayerGameplay.meleeCritical((Player) (Object) this, target, original, charged);
    }

    @ModifyConstant(method = "attack(Lnet/minecraft/world/entity/Entity;)V", constant = @Constant(floatValue = 1.5f))
    private float playerex$criticalMultiplier(float original) {
        return PlayerGameplay.meleeMultiplier((Player) (Object) this, original);
    }
}
