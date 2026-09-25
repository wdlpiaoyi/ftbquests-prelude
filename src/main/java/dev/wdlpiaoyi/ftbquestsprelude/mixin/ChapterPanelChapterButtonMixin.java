package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Makes chapter-button rendering and width calculation work without a client player.
 * Targets the inner {@code ChapterPanel.ChapterButton} class.
 */
@Mixin(ChapterPanel.ChapterButton.class)
public abstract class ChapterPanelChapterButtonMixin {

    @Redirect(method = "draw", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/client/player/LocalPlayer;getUUID()Ljava/util/UUID;"))
    private UUID prelude$draw(LocalPlayer player) {
        return player == null ? Util.NIL_UUID : player.getUUID();
    }

    @Redirect(method = "getActualWidth", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/client/player/LocalPlayer;getUUID()Ljava/util/UUID;"))
    private UUID prelude$width(LocalPlayer player) {
        return player == null ? Util.NIL_UUID : player.getUUID();
    }
}
