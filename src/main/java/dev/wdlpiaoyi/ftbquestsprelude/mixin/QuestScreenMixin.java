package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Makes the debug info tooltip ({@code addInfoTooltip}) work without a client player.
 */
@Mixin(QuestScreen.class)
public abstract class QuestScreenMixin {

    @Redirect(method = "addInfoTooltip", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/entity/player/Player;getUUID()Ljava/util/UUID;"))
    private UUID prelude$tooltip(Player player) {
        return player == null ? Util.NIL_UUID : player.getUUID();
    }
}
