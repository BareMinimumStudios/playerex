package com.bibireden.playerex.mixin;

import com.bibireden.playerex.progression.PlayerGameplay;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class NeoForgePlayerCombatMixin {
    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;fireCriticalHit(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;ZF)Lnet/neoforged/neoforge/event/entity/player/CriticalHitEvent;", remap = false))
    private CriticalHitEvent playerex$critical(Player player, Entity target, boolean vanillaCritical, float multiplier,
            Operation<CriticalHitEvent> original, @Local(ordinal = 0) boolean charged) {
        boolean critical = PlayerGameplay.meleeCritical(player, target, vanillaCritical, charged);
        float modified = critical ? PlayerGameplay.meleeMultiplier(player, multiplier) : multiplier;
        // Keep the native event in the path so other NeoForge mods can veto or modify the result.
        return original.call(player, target, critical, modified);
    }
}
