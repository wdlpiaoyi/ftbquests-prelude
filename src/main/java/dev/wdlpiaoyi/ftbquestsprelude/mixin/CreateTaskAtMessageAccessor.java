package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.CreateTaskAtMessage;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CreateTaskAtMessage.class, remap = false)
public interface CreateTaskAtMessageAccessor {
    @Accessor("chapterId")
    long prelude$getChapterId();

    @Accessor("x")
    double prelude$getX();

    @Accessor("y")
    double prelude$getY();

    @Accessor("type")
    TaskType prelude$getType();

    @Accessor("nbt")
    CompoundTag prelude$getNbt();
}
