package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.ChangeChapterGroupMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChangeChapterGroupMessage.class, remap = false)
public interface ChangeChapterGroupMessageAccessor {
    @Accessor("chapterId")
    long prelude$getChapterId();

    @Accessor("groupId")
    long prelude$getGroupId();
}
