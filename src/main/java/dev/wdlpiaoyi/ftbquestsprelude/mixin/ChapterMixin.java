package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.quest.Chapter;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Preserves the on-disk file name of a chapter when its {@code .snbt} file has no {@code filename}
 * field.
 *
 * <p>{@code Chapter.readData} assigns {@code filename = nbt.getString("filename")}, which is empty
 * for hand-written / legacy files, so {@code getFilename()} falls back to the numeric id and the
 * next save renames the file. Keeping the name the file was loaded from avoids surprising renames
 * (and, with the local editor's orphan cleanup, avoids a spurious delete + rename cycle).
 */
@Mixin(value = Chapter.class, remap = false)
public abstract class ChapterMixin {

    @Shadow
    private String filename;

    @Unique
    private String prelude$loadedFilename;

    @Inject(method = "readData", remap = false, at = @At("HEAD"))
    private void prelude$captureFilename(CompoundTag nbt, CallbackInfo ci) {
        prelude$loadedFilename = filename;
    }

    @Inject(method = "readData", remap = false, at = @At("RETURN"))
    private void prelude$restoreFilename(CompoundTag nbt, CallbackInfo ci) {
        if ((filename == null || filename.isEmpty())
                && prelude$loadedFilename != null && !prelude$loadedFilename.isEmpty()) {
            filename = prelude$loadedFilename;
        }
    }
}
