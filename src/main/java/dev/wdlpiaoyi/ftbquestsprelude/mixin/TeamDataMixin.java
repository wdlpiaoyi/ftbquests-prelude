package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Makes {@link TeamData}'s per-player lookups null-safe.
 *
 * <p>Outside a world there is no client player ({@code Minecraft.player == null}) but the native
 * quest GUI still queries per-player state such as "is this quest pinned". With a null player the
 * lookup now falls through and simply returns no data instead of throwing.
 */
@Mixin(TeamData.class)
public abstract class TeamDataMixin {

    @Redirect(method = "getOrCreatePlayerData", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/entity/player/Player;getUUID()Ljava/util/UUID;"))
    private UUID prelude$safePlayerUuid(Player player) {
        return player == null ? Util.NIL_UUID : player.getUUID();
    }
}
