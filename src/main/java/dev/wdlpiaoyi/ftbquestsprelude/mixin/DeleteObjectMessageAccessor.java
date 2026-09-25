package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DeleteObjectMessage.class, remap = false)
public interface DeleteObjectMessageAccessor {
    @Accessor("id")
    long prelude$getId();
}
