package com.bibireden.playerex.mixin;

import com.bibireden.playerex.PlayerEX;
import com.bibireden.playerex.item.PlayerExItemComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(AnvilMenu.class)
abstract class AnvilRelicMixin extends ItemCombinerMenu {
    @Shadow private String itemName;
    @Shadow private int repairItemCountCost;
    @Shadow @Final private DataSlot cost;

    private AnvilRelicMixin(MenuType<?> type, int id, Inventory inventory, ContainerLevelAccess access) {
        super(type, id, inventory, access);
    }

    @Unique private boolean playerex$canInfuse() {
        ItemStack target = inputSlots.getItem(0), donor = inputSlots.getItem(1);
        if (!(target.getItem() instanceof ArmorItem armor) || !(donor.getItem() instanceof ArmorItem relic)) return false;
        var donorId = BuiltInRegistries.ITEM.getKey(donor.getItem());
        var targetId = BuiltInRegistries.ITEM.getKey(target.getItem());
        return (donorId.equals(PlayerEX.id("head_relic")) || donorId.equals(PlayerEX.id("chest_relic")))
            && !targetId.equals(PlayerEX.id("head_relic")) && !targetId.equals(PlayerEX.id("chest_relic"))
            && armor.getEquipmentSlot() == relic.getEquipmentSlot()
            && !target.has(PlayerExItemComponents.RELIC) && donor.has(PlayerExItemComponents.RELIC);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void playerex$infuse(CallbackInfo ci) {
        if (!playerex$canInfuse()) return;
        ItemStack result = inputSlots.getItem(0).copy();
        result.set(PlayerExItemComponents.RELIC, inputSlots.getItem(1).get(PlayerExItemComponents.RELIC));
        if (itemName != null && !itemName.isBlank()) result.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
        cost.set(0);
        repairItemCountCost = 1;
        resultSlots.setItem(0, result);
        broadcastChanges();
        ci.cancel();
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void playerex$pickup(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (playerex$canInfuse()) cir.setReturnValue(present && !resultSlots.getItem(0).isEmpty());
    }
}
