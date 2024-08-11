package com.bibireden.playerex.mixin;

import com.bibireden.data_attributes.api.DataAttributesAPI;
import com.bibireden.playerex.PlayerEX;
import com.bibireden.playerex.api.attribute.PlayerEXAttributes;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin<T extends Entity> {
    @Unique
    private boolean playerex$shouldRenderLevel() { return PlayerEX.CONFIG.getShowLevelOnNameplates(); }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"), index = 1)
    private Component playerex$renderLabelIfPresent(Component text, @Local(argsOnly = true) Entity entity) {
        if (playerex$shouldRenderLevel() && entity instanceof Player livingEntity) {
            Optional<Double> maybeLevel = DataAttributesAPI.getValue(PlayerEXAttributes.LEVEL, livingEntity);
            if (maybeLevel.isPresent()) {
                text = text.copy().append(" ").append(
                    Component.translatable("playerex.ui.nameplate.level", maybeLevel.get().intValue())
                        .withStyle((style) -> style.withColor(PlayerEX.CONFIG.getVisualSettings().getNameplateColor().rgb()))
                );
            }
        }
        return text;
    }
}
