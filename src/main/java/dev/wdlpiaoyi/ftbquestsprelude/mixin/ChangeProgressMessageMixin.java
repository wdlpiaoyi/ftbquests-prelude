package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.ChangeProgressMessage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Guards {@code ChangeProgressMessage.sendToServer(TeamData, QuestObjectBase, Consumer)}, a static
 * helper that dereferences the client player while building the message.
 *
 * <p>Outside a world there is no client player, so the helper would throw before the message could
 * even be intercepted. Progress changes are not supported by the local editor, so simply skipping
 * the call is correct.
 */
@Mixin(ChangeProgressMessage.class)
public abstract class ChangeProgressMessageMixin {

    @Inject(method = "sendToServer(Ldev/ftb/mods/ftbquests/quest/TeamData;Ldev/ftb/mods/ftbquests/quest/QuestObjectBase;Ljava/util/function/Consumer;)V",
            remap = false, at = @At("HEAD"), cancellable = true)
    private static void prelude$guardNoPlayer(TeamData team, QuestObjectBase object,
                                              Consumer<?> progressChange, CallbackInfo ci) {
        if (Minecraft.getInstance().getConnection() == null) {
            ci.cancel();
        }
    }
}
