package com.bibireden.playerex.mixin;

import com.bibireden.playerex.item.RelicLoot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class LootTableRelicMixin {
    @Shadow @Final private java.util.function.BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void playerex$chestLoot(LootContext context, Consumer<ItemStack> consumer, CallbackInfo ci) {
        if (((LootTable) (Object) this).getParamSet() == LootContextParamSets.CHEST) {
            RelicLoot.chest(context, stack -> consumer.accept(compositeFunction.apply(stack, context)));
        }
    }
}
