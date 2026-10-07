package com.bibireden.playerex.mixin.client;

import com.bibireden.playerex.PlayerEX;
import com.bibireden.playerex.client.RelicArmorModels;
import com.bibireden.playerex.item.PlayerExItemComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
abstract class RelicArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    @Unique private static final ResourceLocation PLAYEREX_HEAD_RELIC = PlayerEX.id("head_relic");
    @Unique private static final ResourceLocation PLAYEREX_CHEST_RELIC = PlayerEX.id("chest_relic");
    private RelicArmorLayerMixin(RenderLayerParent<T, M> parent) { super(parent); }
    @Shadow protected abstract void setPartVisibility(A model, EquipmentSlot slot);
    @Shadow private void renderModel(PoseStack pose, MultiBufferSource buffers, int light, A model, int color, ResourceLocation texture) { throw new AssertionError(); }
    @Shadow private void renderTrim(Holder<ArmorMaterial> material, PoseStack pose, MultiBufferSource buffers, int light, ArmorTrim trim, A model, boolean inner) { throw new AssertionError(); }
    @Shadow private void renderGlint(PoseStack pose, MultiBufferSource buffers, int light, A model) { throw new AssertionError(); }

    @SuppressWarnings("unchecked")
    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void playerex$relicArmor(PoseStack pose, MultiBufferSource buffers, T entity, EquipmentSlot slot, int light, A original, CallbackInfo ci) {
        ItemStack stack = entity.getItemBySlot(slot);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.equals(PLAYEREX_HEAD_RELIC) && !id.equals(PLAYEREX_CHEST_RELIC)) return;
        if (!(stack.getItem() instanceof ArmorItem armor) || armor.getEquipmentSlot() != slot) return;
        var state = stack.get(PlayerExItemComponents.RELIC);
        var models = RelicArmorModels.get(state == null ? 0 : state.getRarity());
        A model = (A) models.getArmor(), trimModel = (A) models.getTrim();
        getParentModel().copyPropertiesTo(model);
        getParentModel().copyPropertiesTo(trimModel);
        setPartVisibility(model, slot);
        setPartVisibility(trimModel, slot);
        renderModel(pose, buffers, light, model, -1, models.getTexture());
        var trim = stack.get(DataComponents.TRIM);
        if (trim != null) renderTrim(armor.getMaterial(), pose, buffers, light, trim, trimModel, false);
        if (stack.hasFoil()) renderGlint(pose, buffers, light, model);
        ci.cancel();
    }
}
