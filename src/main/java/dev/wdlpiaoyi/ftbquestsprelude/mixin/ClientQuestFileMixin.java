package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Complements the local quest book:
 * <ul>
 *   <li>guards {@link ClientQuestFile#isPlayerOnTeam(Player, TeamData)} against a null player; and</li>
 *   <li>makes {@link ClientQuestFile#canEdit()} return true while the local file is in editor mode,
 *       since the built-in check requires a client player that does not exist outside a world.</li>
 * </ul>
 */
@Mixin(ClientQuestFile.class)
public abstract class ClientQuestFileMixin {

    @Inject(method = "isPlayerOnTeam", remap = false, at = @At("HEAD"), cancellable = true)
    private void prelude$noPlayer(Player player, TeamData teamData, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canEdit", remap = false, at = @At("HEAD"), cancellable = true)
    private void prelude$localEditor(CallbackInfoReturnable<Boolean> cir) {
        if (LocalQuestSession.isEditing((ClientQuestFile) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
