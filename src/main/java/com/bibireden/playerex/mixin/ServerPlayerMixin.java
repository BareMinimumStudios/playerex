package com.bibireden.playerex.mixin;

import com.bibireden.playerex.components.PlayerEXComponents;
import com.bibireden.playerex.components.player.PlayerDataComponent;
import com.bibireden.playerex.factory.ServerNetworkingFactory;
import com.bibireden.playerex.networking.types.NotificationType;
import com.bibireden.playerex.util.PlayerEXUtil;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnreachableCode")
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "giveExperienceLevels", at = @At("TAIL"))
    private void addExperienceLevels(int levels, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        PlayerDataComponent component = (PlayerDataComponent) PlayerEXComponents.PLAYER_DATA.get(player);

        int current = player.experienceLevel;
        int required = PlayerEXUtil.getRequiredXpForNextLevel(player);

        if (current >= required) {
            if (!component.isLevelUpNotified()) {
                component.setLevelUpNotified(true);
                ServerNetworkingFactory.notify(player, NotificationType.LevelUpAvailable);
            }
        }
        else {
            component.setLevelUpNotified(false);
        }
    }
}
