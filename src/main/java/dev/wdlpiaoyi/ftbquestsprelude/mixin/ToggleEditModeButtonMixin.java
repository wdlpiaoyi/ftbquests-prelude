package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.OtherButtonsPanelBottom;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces FTB Quests' edit-mode button tooltip (which mentions a team name) with a clear
 * on/off description while the local quest book is open.
 */
@Mixin(OtherButtonsPanelBottom.ToggleEditModeButton.class)
public abstract class ToggleEditModeButtonMixin {

    @Inject(method = "makeTooltip", remap = false, at = @At("HEAD"), cancellable = true)
    private static void prelude$tooltip(CallbackInfoReturnable<Component> cir) {
        if (LocalQuestSession.isActive()) {
            cir.setReturnValue(Component.translatable(LocalQuestSession.isEditing()
                    ? "ftbquests_prelude.editing_mode.on"
                    : "ftbquests_prelude.editing_mode.off"));
        }
    }
}
