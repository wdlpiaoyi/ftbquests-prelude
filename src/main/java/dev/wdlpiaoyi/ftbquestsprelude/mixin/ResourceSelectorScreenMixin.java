package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftblibrary.config.ui.ResourceSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.ResourceSelectorScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.wdlpiaoyi.ftbquestsprelude.client.RegistryItemSearchMode;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Swaps the item selector's "all items" data source while the local quest book is open outside a world.
 *
 * <p>FTB Library's {@code ALL_ITEMS} mode lists creative tab contents, which are only populated after a
 * world load, so without this the item picker is empty and item tasks/rewards cannot be created at the
 * main menu. Only the data source changes - the screen, its search modes and FTB Quests' config flow
 * are untouched.
 */
@Mixin(value = ResourceSelectorScreen.class, remap = false)
public abstract class ResourceSelectorScreenMixin {

    @Inject(method = "getActiveSearchMode", remap = false, at = @At("RETURN"), cancellable = true)
    private void prelude$registryItems(CallbackInfoReturnable<Optional<ResourceSearchMode>> cir) {
        // In a world the creative tabs are populated, so the vanilla path is fine.
        if (!LocalQuestSession.isLocalBook() || Minecraft.getInstance().level != null) {
            return;
        }
        if (!((Object) this instanceof SelectItemStackScreen)) {
            return;
        }

        Optional<ResourceSearchMode> mode = cir.getReturnValue();
        if (mode.isEmpty() || mode.get() != ResourceSearchMode.ALL_ITEMS) {
            return;
        }

        cir.setReturnValue(Optional.of(RegistryItemSearchMode.INSTANCE));
    }
}
