package com.bibireden.playerex.mixin;

import com.bibireden.playerex.components.PlayerEXComponents;
import com.bibireden.playerex.components.player.PlayerDataComponent;
import com.bibireden.playerex.factory.ServerNetworkingFactory;
import com.bibireden.playerex.networking.types.NotificationType;
import com.bibireden.playerex.util.PlayerEXUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "giveExperienceLevels", at = @At("TAIL"))
    private void addExperienceLevels(int levels, CallbackInfo ci) {
        PlayerDataComponent component = (PlayerDataComponent) PlayerEXComponents.PLAYER_DATA.get(this);

        int current = this.experienceLevel;
        int required = PlayerEXUtil.getRequiredXpForNextLevel(this);

        if (current >= required) {
            if (!component.isLevelUpNotified()) {
                component.setLevelUpNotified(true);
                ServerNetworkingFactory.notify(this, NotificationType.LevelUpAvailable);
            }
        }
        else {
            component.setLevelUpNotified(false);
        }
    }
}
