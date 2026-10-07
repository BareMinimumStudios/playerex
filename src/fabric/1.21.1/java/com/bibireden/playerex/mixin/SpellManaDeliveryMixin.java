package com.bibireden.playerex.mixin;

import com.bibireden.playerex.compat.mana.FabricSpellMana;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;
import java.util.function.Consumer;

@Pseudo
@Mixin(targets = "net.spell_engine.internals.delivery.SpellDelivery", remap = false)
public abstract class SpellManaDeliveryMixin {
    @WrapMethod(method = "resolveAndDeliver")
    private static boolean playerex$manaDelivery(@Coerce Object world, @Coerce Object caster,
                                                 @Coerce Object entry, @Coerce Object targets,
                                                 @Coerce Object context, Consumer<Object> completion,
                                                 Operation<Boolean> original) {
        return FabricSpellMana.deliver(caster, entry, completion,
                callback -> original.call(world, caster, entry, targets, context, callback));
    }
}
