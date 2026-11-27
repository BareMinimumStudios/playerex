package com.bibireden.playerex.mixin.client;

import com.bibireden.playerex.client.EquipmentTooltipPresentation;
import com.bibireden.playerex.item.ItemProgression;
import com.bibireden.playerex.item.RelicEquipment;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import kotlin.Pair;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class EquipmentTooltipMixin {
    @Inject(method = "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At("RETURN"))
    private void playerex$itemProgression(Item.TooltipContext context, Player player,
            TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        var rarityColor = RelicEquipment.rarityColor((ItemStack) (Object) this);
        if (rarityColor != null && !cir.getReturnValue().isEmpty()) {
            cir.getReturnValue().set(0, cir.getReturnValue().getFirst().copy().withStyle(rarityColor));
        }
        ItemProgression.tooltip((ItemStack) (Object) this, cir.getReturnValue(), flag.isAdvanced(),
            !EquipmentTooltipPresentation.getHoldShift() || Screen.hasShiftDown());
    }
    @WrapOperation(method = "addAttributeTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"))
    private void playerex$mergeProgressionRows(ItemStack stack, EquipmentSlotGroup group,
            BiConsumer<Holder<Attribute>, AttributeModifier> consumer,
            Operation<Void> original) {
        List<Pair<Holder<Attribute>, AttributeModifier>> rows = new ArrayList<>();
        original.call(stack, group, (BiConsumer<Holder<Attribute>, AttributeModifier>)
                (attribute, modifier) -> rows.add(new Pair<>(attribute, modifier)));
        EquipmentTooltipPresentation.modifiers(rows, consumer);
    }
    @Inject(method = "addModifierTooltip", at = @At("HEAD"), cancellable = true)
    private void playerex$equipmentTooltip(Consumer<Component> consumer, Player player,
            Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo ci) {
        if (EquipmentTooltipPresentation.append(consumer, player, attribute, modifier)) ci.cancel();
    }
}
