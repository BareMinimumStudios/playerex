package com.bibireden.playerex.mixin;

import com.bibireden.playerex.compat.mana.FabricSpellMana;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "net.spell_engine.internals.SpellExecution", remap = false)
public abstract class SpellManaExecutionMixin {
    @WrapMethod(method = "performSpell")
    private static void playerex$manaCast(@Coerce Object world, @Coerce Object player,
                                         @Coerce Object entry, @Coerce Object targets,
                                         @Coerce Object action, float progress, Operation<Void> original) {
        var previous = FabricSpellMana.begin(player, entry, action);
        try { original.call(world, player, entry, targets, action, progress); }
        finally { FabricSpellMana.end(previous); }
    }
}
