package com.bibireden.playerex.mixin;

import com.bibireden.playerex.ui.PlayerEXScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Shadow @Final protected Minecraft minecraft;

    @Inject(method = "setExperienceValues", at = @At("TAIL"))
    private void setExperience(CallbackInfo ci) {
        if (minecraft.screen instanceof PlayerEXScreen screen) screen.onExperienceUpdated();
    }
}
