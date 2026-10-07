package com.bibireden.playerex.mixin;

import com.bibireden.playerex.compat.mana.FabricSpellMana;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "net.spell_engine.internals.cost.Ammo", remap = false)
public abstract class SpellManaAmmoMixin {
    @Coerce
    @WrapMethod(method = "ammoForSpell")
    private static Object playerex$manaAmmo(@Coerce Object player, @Coerce Object spell,
                                           @Coerce Object stack, Operation<Object> original) {
        return FabricSpellMana.ammo(player, spell, original.call(player, spell, stack));
    }
}
