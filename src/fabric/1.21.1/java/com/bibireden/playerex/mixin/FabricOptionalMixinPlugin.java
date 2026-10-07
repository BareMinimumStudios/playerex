package com.bibireden.playerex.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Set;

public final class FabricOptionalMixinPlugin implements IMixinConfigPlugin {
    @Override public boolean shouldApplyMixin(String target, String mixin) {
        if (!mixin.endsWith("SpellManaCostMixin") && !mixin.endsWith("SpellManaAmmoMixin")
                && !mixin.endsWith("SpellManaExecutionMixin") && !mixin.endsWith("SpellManaDeliveryMixin")) return true;
        var loader = FabricLoader.getInstance();
        return loader.isModLoaded("manaattributes") && loader.isModLoaded("spell_engine");
    }
    @Override public void onLoad(String mixinPackage) { }
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> mine, Set<String> others) { }
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) { }
    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) { }
}
