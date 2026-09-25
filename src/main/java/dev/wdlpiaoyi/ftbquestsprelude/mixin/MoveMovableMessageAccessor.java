package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.MoveMovableMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MoveMovableMessage.class, remap = false)
public interface MoveMovableMessageAccessor {
    @Accessor("id")
    long prelude$getId();

    @Accessor("chapterID")
    long prelude$getChapterId();

    @Accessor("x")
    double prelude$getX();

    @Accessor("y")
    double prelude$getY();
}
