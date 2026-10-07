package com.bibireden.playerex.mixin.client;

import com.bibireden.playerex.PlayerEX;
import com.bibireden.playerex.item.ItemProgression;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
abstract class BrokenItemOverlayMixin {
    @Unique private static final ResourceLocation PLAYEREX_BROKEN = PlayerEX.id("textures/gui/broken.png");
    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At("TAIL"))
    private void playerex$brokenOverlay(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int offset, CallbackInfo ci) {
        if (!ItemProgression.isBroken(stack)) return;
        GuiGraphics graphics = (GuiGraphics) (Object) this;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        graphics.blit(PLAYEREX_BROKEN, x + 7, y + 1, 0f, 0f, 8, 8, 8, 8);
        graphics.pose().popPose();
    }
}
