package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.client.gui.quests.CollectRewardsButton;
import dev.ftb.mods.ftbquests.client.gui.quests.OtherButtonsPanelTop;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the buttons that only make sense against a server while the local quest book is open.
 *
 * <p>"Collect rewards" and the auto-pin toggle both need a server to act on: outside a world the
 * first one just opens a dead screen and the second one silently does nothing. The editor settings /
 * preferences / key-reference buttons stay, since those work locally.
 */
@Mixin(OtherButtonsPanelTop.class)
public abstract class OtherButtonsPanelTopMixin {

    @Inject(method = "addWidgets", remap = false, at = @At("TAIL"))
    private void prelude$hideServerOnlyButtons(CallbackInfo ci) {
        if (!LocalQuestSession.isLocalBook()) {
            return;
        }
        Panel self = (Panel) (Object) this;
        self.getWidgets().removeIf(widget -> widget instanceof CollectRewardsButton
                || widget instanceof OtherButtonsPanelTop.AutopinButton);
    }
}
