package com.bibireden.playerex.mixin;

import com.bibireden.playerex.compat.mana.SpellManaCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net.spell_engine.api.spell.Spell$Cost", remap = false)
public abstract class SpellManaCostMixin implements SpellManaCost {
    // Gson's public-field schema allows datapacks to set cost.rpgmana.
    @Unique public float rpgmana = -1f;

    @Override public float playerex$manaCost() { return rpgmana; }
}
