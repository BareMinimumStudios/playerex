package com.bibireden.playerex.mixin.client;

import com.bibireden.playerex.client.EquipmentTooltipPresentation;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class EquipmentTooltipMixin {
    @Inject(method = "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At("RETURN"))
    private void playerex$itemProgression(net.minecraft.world.item.Item.TooltipContext context, Player player,
            net.minecraft.world.item.TooltipFlag flag, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<java.util.List<Component>> cir) {
        var rarityColor = com.bibireden.playerex.item.RelicEquipment.rarityColor((ItemStack) (Object) this);
        if (rarityColor != null && !cir.getReturnValue().isEmpty()) {
            cir.getReturnValue().set(0, cir.getReturnValue().getFirst().copy().withStyle(rarityColor));
        }
        com.bibireden.playerex.item.ItemProgression.tooltip((ItemStack) (Object) this, cir.getReturnValue(), flag.isAdvanced(),
            !EquipmentTooltipPresentation.getHoldShift() || net.minecraft.client.gui.screens.Screen.hasShiftDown());
    }
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "addAttributeTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"))
    private void playerex$mergeProgressionRows(ItemStack stack, net.minecraft.world.entity.EquipmentSlotGroup group,
            java.util.function.BiConsumer<Holder<Attribute>, AttributeModifier> consumer,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        java.util.List<kotlin.Pair<Holder<Attribute>, AttributeModifier>> rows = new java.util.ArrayList<>();
        original.call(stack, group, (java.util.function.BiConsumer<Holder<Attribute>, AttributeModifier>)
                (attribute, modifier) -> rows.add(new kotlin.Pair<>(attribute, modifier)));
        EquipmentTooltipPresentation.modifiers(rows, consumer);
    }
    @Inject(method = "addModifierTooltip", at = @At("HEAD"), cancellable = true)
    private void playerex$equipmentTooltip(Consumer<Component> consumer, Player player,
            Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo ci) {
        if (EquipmentTooltipPresentation.append(consumer, player, attribute, modifier)) ci.cancel();
    }
}
