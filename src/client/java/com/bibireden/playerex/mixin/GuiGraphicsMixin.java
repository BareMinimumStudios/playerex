package com.bibireden.playerex.mixin;

import com.bibireden.playerex.util.PlayerEXUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
abstract class GuiGraphicsMixin {
    @Shadow
    abstract void innerBlit(ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV, float red, float green, float blue, float alpha);

    @Shadow
    @Final
    private PoseStack pose;

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At(value = "TAIL"))
    public void renderBrokenItem(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int guiOffset, CallbackInfo ci) {
        if (PlayerEXUtil.isBroken(stack)) {
            this.pose.pushPose();
            int size = 8;
            this.pose.translate(15 - size, 1, 300);
            innerBlit(
                    new ResourceLocation("playerex", "textures/gui/broken.png"),
                    x, x + size, y, y + size,
                    0, 0, 1, 0, 1,
                    1f, 1f, 1f, 1f);
            this.pose.popPose();
        }
    }
}
