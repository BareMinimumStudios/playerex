package com.bibireden.playerex.mixin;

import com.bibireden.data_attributes.api.DataAttributesAPI;
import com.bibireden.playerex.api.attribute.PlayerEXAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
    // Constructor for the mixin class
    private AbstractArrowMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;isCritArrow()Z"))
    private void playerex_onEntityHit(EntityHitResult entityHitResult, CallbackInfo info) {
        AbstractArrow projectileEntity = (AbstractArrow)(Object)this;
        Entity owner = projectileEntity.getOwner();

        if(owner instanceof LivingEntity) {

            Optional<Double> rangedCritChanceOptional = DataAttributesAPI.getValue(PlayerEXAttributes.RANGED_CRITICAL_CHANCE, (LivingEntity)owner);

            if (rangedCritChanceOptional.isPresent())
            {
                projectileEntity.setCritArrow(false);
            }
        }
    }

    @SuppressWarnings("UnreachableCode")
    @ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float playerex_onEntityHit(float i) {
        AbstractArrow projectileEntity = (AbstractArrow)(Object) this;
        Entity owner = projectileEntity.getOwner();
        double damage = i;

        if(owner instanceof LivingEntity livingEntity) {
            Optional<Double> damageOptional = DataAttributesAPI.getValue(PlayerEXAttributes.RANGED_DAMAGE, livingEntity);

            damage = damageOptional.isPresent() ? damageOptional.get() + i : i;

            final double amount = damage;

            Optional<Double> rangedCritOptional = DataAttributesAPI.getValue(PlayerEXAttributes.RANGED_CRITICAL_CHANCE, livingEntity);

            if (rangedCritOptional.isPresent())
            {
                boolean cache = livingEntity.getRandom().nextFloat() < rangedCritOptional.get();
                projectileEntity.setCritArrow(cache);

                if (cache)
                {
                    Optional<Double> rangedCritDamageOptional = DataAttributesAPI.getValue(PlayerEXAttributes.RANGED_CRITICAL_DAMAGE, livingEntity);
                    if (rangedCritOptional.isPresent())
                    {
                        damage = amount * (1.0 + (10.0 * rangedCritDamageOptional.get()));
                    }
                }
            }
        }

        return (float) damage;
    }
}
