package com.bibireden.playerex.mixin;

import com.bibireden.data_attributes.api.DataAttributesAPI;
import com.bibireden.playerex.api.attribute.PlayerEXAttributes;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow @Final public Player player;

    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float playerex$getBlockBreakingSpeed(float original) {
        Optional<Double> maybeBreakingSpeed = DataAttributesAPI.getValue(PlayerEXAttributes.BREAKING_SPEED, this.player);
        return maybeBreakingSpeed.map(v -> original + v.floatValue() - 1.0F).orElse(original);
    }
}
