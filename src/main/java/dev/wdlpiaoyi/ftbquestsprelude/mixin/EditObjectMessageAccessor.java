package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EditObjectMessage.class, remap = false)
public interface EditObjectMessageAccessor {
    @Accessor("id")
    long prelude$getId();

    @Accessor("nbt")
    CompoundTag prelude$getNbt();
}
