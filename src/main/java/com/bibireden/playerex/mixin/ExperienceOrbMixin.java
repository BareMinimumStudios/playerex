package com.bibireden.playerex.mixin;

import com.bibireden.playerex.components.PlayerEXComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends Entity {
    public ExperienceOrbMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDI)V", at = @At("TAIL"))
    private void playerex$init(Level world, double x, double y, double z, int amount, CallbackInfo ci) {
        BlockPos pos = BlockPos.containing(x, y, z);
        ChunkAccess chunk = world.getChunk(pos);
        PlayerEXComponents.EXPERIENCE_DATA.maybeGet(chunk).ifPresent(data -> {
            if (data.updateExperienceNegationFactor(amount)) {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        });
    }
}
