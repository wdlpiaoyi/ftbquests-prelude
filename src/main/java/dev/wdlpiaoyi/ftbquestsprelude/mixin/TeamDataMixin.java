package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Makes {@link TeamData}'s per-player lookups work without a client player.
 *
 * <p>Outside a world {@code Minecraft.player} is null, but the native quest GUI still queries
 * per-player state such as "is this quest pinned". A single-player save is keyed by the real player
 * UUID, so a null player resolves to the local account's UUID rather than a placeholder that would
 * match nothing.
 */
@Mixin(TeamData.class)
public abstract class TeamDataMixin {

    @Redirect(method = "getOrCreatePlayerData", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/entity/player/Player;getUUID()Ljava/util/UUID;"))
    private UUID prelude$safePlayerUuid(Player player) {
        return player == null ? LocalQuestSession.viewPlayerUuid() : player.getUUID();
    }
}
