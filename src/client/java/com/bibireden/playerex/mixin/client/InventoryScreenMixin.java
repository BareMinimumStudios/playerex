package com.bibireden.playerex.mixin.client;

import com.bibireden.playerex.client.InventoryLinkButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Covers vanilla and compatible modded player-inventory screens, without adding tabs to chests. */
@Mixin(AbstractContainerScreen.class)
public abstract class InventoryScreenMixin extends Screen {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;

    protected InventoryScreenMixin(Component title) { super(title); }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void playerex$addProgressionTab(CallbackInfo ci) {
        if (com.bibireden.playerex.config.PlayerExConfigState.current().getDisableUI()) return;
        if (minecraft == null || minecraft.player == null) return;
        Object self = this;
        AbstractContainerScreen<?> container = (AbstractContainerScreen<?>) self;
        boolean creative = self instanceof CreativeModeInventoryScreen;
        if (!creative && !(container.getMenu() instanceof InventoryMenu)) return;
        // Vanilla replaces this temporary screen with its creative screen during init.
        if (self instanceof InventoryScreen && minecraft.player.getAbilities().instabuild) return;
        addRenderableWidget(new InventoryLinkButton(() -> leftPos + imageWidth - 1, () -> topPos + 35));
    }
}
