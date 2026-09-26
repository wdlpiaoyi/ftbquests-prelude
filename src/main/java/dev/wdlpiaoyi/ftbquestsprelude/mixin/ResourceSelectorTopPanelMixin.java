package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the item selector's search-mode button while the local quest book is open.
 *
 * <p>The "all items" mode is replaced by a registry-backed one (see {@code ResourceSelectorScreenMixin}
 * and {@code RegistryItemSearchMode}); the remaining modes ("inventory" and anything a recipe viewer
 * adds) are meaningless without a world, so leaving a button that cycles to a dead mode is worse than
 * not offering it.
 *
 * <p>{@code SearchModeButton} is package-private, so it is matched by simple name.
 */
@Mixin(targets = "dev.ftb.mods.ftblibrary.config.ui.ResourceSelectorScreen$CustomTopPanel")
public abstract class ResourceSelectorTopPanelMixin {

    @Inject(method = "addWidgets", remap = false, at = @At("TAIL"))
    private void prelude$hideSearchModeButton(CallbackInfo ci) {
        if (!LocalQuestSession.isLocalBook()) {
            return;
        }
        Panel self = (Panel) (Object) this;
        if (!(self.getGui() instanceof SelectItemStackScreen)) {
            return;
        }
        self.getWidgets().removeIf(widget -> widget.getClass().getSimpleName().equals("SearchModeButton"));
    }
}
