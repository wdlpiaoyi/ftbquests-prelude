package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.OtherButtonsPanelBottom;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Guards the operator-permission check in the bottom button panel, which otherwise dereferences a
 * null client player when the screen is opened outside a world.
 */
@Mixin(OtherButtonsPanelBottom.class)
public abstract class OtherButtonsPanelBottomMixin {

    @Redirect(method = "addWidgets", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/entity/player/Player;hasPermissions(I)Z"))
    private boolean prelude$safeHasPermissions(Player player, int level) {
        return player != null && player.hasPermissions(level);
    }
}
