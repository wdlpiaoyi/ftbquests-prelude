package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.ReorderItemMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ReorderItemMessage.class, remap = false)
public interface ReorderItemMessageAccessor {
    @Accessor("id")
    long prelude$getId();

    @Accessor("moveRight")
    boolean prelude$getMoveRight();
}
