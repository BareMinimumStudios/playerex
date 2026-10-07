package com.bibireden.playerex.mixin;

import com.bibireden.playerex.progression.PlayerGameplay;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public abstract class InventoryGameplayMixin {
    @Shadow @Final public Player player;
    @ModifyReturnValue(method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F", at = @At("RETURN"))
    private float playerex$breakingSpeed(float original) { return PlayerGameplay.breakingSpeed(player, original); }
}