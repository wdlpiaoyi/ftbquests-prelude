package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Guards {@link ClientQuestFile#isPlayerOnTeam(Player, TeamData)} against a null player.
 */
@Mixin(ClientQuestFile.class)
public abstract class ClientQuestFileMixin {

    @Inject(method = "isPlayerOnTeam", remap = false, at = @At("HEAD"), cancellable = true)
    private void prelude$noPlayer(Player player, TeamData teamData, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) {
            cir.setReturnValue(false);
        }
    }
}
