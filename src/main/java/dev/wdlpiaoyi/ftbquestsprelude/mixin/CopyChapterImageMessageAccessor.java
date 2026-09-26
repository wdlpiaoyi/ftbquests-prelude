package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.net.CopyChapterImageMessage;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the payload of {@code CopyChapterImageMessage} so {@code LocalEditBridge} can add the
 * copied chapter image to the local chapter.
 */
@Mixin(value = CopyChapterImageMessage.class, remap = false)
public interface CopyChapterImageMessageAccessor {

    @Accessor("img")
    ChapterImage prelude$getImage();

    @Accessor("chapterId")
    long prelude$getChapterId();
}
