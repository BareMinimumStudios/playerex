package com.bibireden.playerex.mixin;

import com.bibireden.playerex.progression.PlayerProgression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import com.bibireden.playerex.state.LoadedPlayerHealth;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerProgressionMixin implements LoadedPlayerHealth {
    @Unique private Float playerex$loadedHealth;

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void playerex$rememberLoadedHealth(CompoundTag tag, CallbackInfo ci) {
        // Vanilla has already clamped Health against the unreconciled maximum.
        float saved = tag.getFloat("Health");
        playerex$loadedHealth = tag.contains("Health", 99) && Float.isFinite(saved) && saved >= 0 ? saved : null;
    }

    @Override
    public void playerex$restoreLoadedHealth() {
        Float saved = playerex$loadedHealth;
        playerex$loadedHealth = null;
        if (saved != null) ((ServerPlayer) (Object) this).setHealth(saved);
    }
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void playerex$availability(CallbackInfo ci) {
        PlayerProgression.checkLevelAvailability((ServerPlayer) (Object) this);
    }
}