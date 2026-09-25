package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.MoveChapterMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MoveChapterMessage.class, remap = false)
public interface MoveChapterMessageAccessor {
    @Accessor("id")
    long prelude$getId();

    @Accessor("movingUp")
    boolean prelude$getMovingUp();
}
