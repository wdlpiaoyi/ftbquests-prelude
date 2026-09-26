package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.client.gui.quests.OtherButtonsPanelBottom;
import dev.wdlpiaoyi.ftbquestsprelude.client.ReloadButton;
import dev.wdlpiaoyi.ftbquestsprelude.client.SaveProgressButton;
import dev.wdlpiaoyi.ftbquestsprelude.client.ThirdPartyButtons;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import dev.wdlpiaoyi.ftbquestsprelude.config.PreludeConfig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the save progress entry to the native quest screen's bottom-right button panel, and guards
 * the operator-permission check which otherwise dereferences a null client player when the screen
 * is opened outside a world.
 */
@Mixin(OtherButtonsPanelBottom.class)
public abstract class OtherButtonsPanelBottomMixin {

    @Redirect(method = "addWidgets", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/entity/player/Player;hasPermissions(I)Z"))
    private boolean prelude$safeHasPermissions(Player player, int level) {
        return player != null && player.hasPermissions(level);
    }

    @Inject(method = "addWidgets", remap = false, at = @At("TAIL"))
    private void prelude$addLocalBookButtons(CallbackInfo ci) {
        // Only in the local (off-world) book. In a world the native book must stay untouched.
        if (!LocalQuestSession.isLocalBook()) {
            return;
        }
        Panel self = (Panel) (Object) this;
        if (PreludeConfig.COMMON.showSaveProgressButton.get()) {
            self.add(new SaveProgressButton(self));
        }
        self.add(new ReloadButton(self));
    }

    /**
     * Removes third-party buttons in the local book. Runs from {@code alignWidgets} rather than the tail
     * of {@code addWidgets} so it happens after every other mod's {@code addWidgets} injector, whatever
     * their mixin priority.
     */
    @Inject(method = "alignWidgets", remap = false, at = @At("HEAD"))
    private void prelude$hideThirdPartyButtons(CallbackInfo ci) {
        ThirdPartyButtons.hideFrom((Panel) (Object) this);
    }
}
