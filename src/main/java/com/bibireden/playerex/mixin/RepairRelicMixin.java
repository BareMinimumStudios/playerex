package com.bibireden.playerex.mixin;

import com.bibireden.playerex.item.PlayerExItemComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
abstract class RepairRelicMixin {
    // Crafting repair constructs a new stack and would discard rolled attributes.
    @Inject(method = "canCombine", at = @At("HEAD"), cancellable = true)
    private static void playerex$preserveRoll(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (first.has(PlayerExItemComponents.RELIC) || second.has(PlayerExItemComponents.RELIC)) cir.setReturnValue(false);
    }
}
