package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.CopyQuestMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the payload of {@code CopyQuestMessage} ("paste quest") so {@code LocalEditBridge} can
 * mirror the server-side copy locally.
 */
@Mixin(value = CopyQuestMessage.class, remap = false)
public interface CopyQuestMessageAccessor {

    @Accessor("id")
    long prelude$getQuestId();

    @Accessor("chapterId")
    long prelude$getChapterId();

    @Accessor("qx")
    double prelude$getX();

    @Accessor("qy")
    double prelude$getY();

    @Accessor("copyDeps")
    boolean prelude$copyDeps();
}
