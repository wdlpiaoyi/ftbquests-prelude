package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Makes quest-button rendering work without a client player.
 */
@Mixin(QuestButton.class)
public abstract class QuestButtonMixin {

    @Redirect(method = "draw", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/entity/player/Player;getUUID()Ljava/util/UUID;"))
    private UUID prelude$draw(Player player) {
        return player == null ? LocalQuestSession.viewPlayerUuid() : player.getUUID();
    }
}
