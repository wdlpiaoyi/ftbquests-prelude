package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.RewardButton;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Makes reward-button rendering and clicking work without a client player.
 *
 * <p>This matters beyond avoiding an NPE: the button looks up whether the reward was already claimed
 * under the player's UUID, and the save's data is keyed by the real player UUID, so resolving to it
 * (instead of {@code Util.NIL_UUID}) is what makes claimed rewards show as claimed.
 */
@Mixin(RewardButton.class)
public abstract class RewardButtonMixin {

    @Redirect(method = "draw", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/client/player/LocalPlayer;getUUID()Ljava/util/UUID;"))
    private UUID prelude$draw(LocalPlayer player) {
        return player == null ? LocalQuestSession.viewPlayerUuid() : player.getUUID();
    }

    @Redirect(method = "onClicked", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/entity/player/Player;getUUID()Ljava/util/UUID;"))
    private UUID prelude$clicked(Player player) {
        return player == null ? LocalQuestSession.viewPlayerUuid() : player.getUUID();
    }
}
