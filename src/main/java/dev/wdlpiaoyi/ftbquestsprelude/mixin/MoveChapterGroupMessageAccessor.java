package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.MoveChapterGroupMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MoveChapterGroupMessage.class, remap = false)
public interface MoveChapterGroupMessageAccessor {
    @Accessor("id")
    long prelude$getId();

    @Accessor("movingUp")
    boolean prelude$getMovingUp();
}
