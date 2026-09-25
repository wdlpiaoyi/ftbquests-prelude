package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CreateObjectMessage.class, remap = false)
public interface CreateObjectMessageAccessor {
    @Accessor("parent")
    long prelude$getParent();

    @Accessor("type")
    QuestObjectType prelude$getType();

    @Accessor("nbt")
    CompoundTag prelude$getNbt();

    @Accessor("extra")
    CompoundTag prelude$getExtra();
}
