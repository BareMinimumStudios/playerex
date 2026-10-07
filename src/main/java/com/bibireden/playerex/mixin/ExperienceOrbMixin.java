package com.bibireden.playerex.mixin;

import com.bibireden.playerex.progression.ChunkExperienceService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDI)V", at = @At("TAIL"))
    private void playerex$applyChunkExperienceNegation(
        Level level,
        double x,
        double y,
        double z,
        int amount,
        CallbackInfo ci
    ) {
        if (level.isClientSide()) return;

        BlockPos pos = BlockPos.containing(x, y, z);
        if (ChunkExperienceService.shouldDiscardOrb(level, pos, amount, level.getRandom().nextFloat())) {
            ((ExperienceOrb) (Object) this).discard();
        }
    }
}
