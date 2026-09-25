package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.CollectRewardsButton;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Makes the "collect rewards" button render and click without a client player.
 */
@Mixin(CollectRewardsButton.class)
public abstract class CollectRewardsButtonMixin {

    @Redirect(method = "draw", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/client/player/LocalPlayer;getUUID()Ljava/util/UUID;"))
    private UUID prelude$draw(LocalPlayer player) {
        return player == null ? Util.NIL_UUID : player.getUUID();
    }

    @Redirect(method = "onClicked", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/client/player/LocalPlayer;getUUID()Ljava/util/UUID;"))
    private UUID prelude$clicked(LocalPlayer player) {
        return player == null ? Util.NIL_UUID : player.getUUID();
    }
}
