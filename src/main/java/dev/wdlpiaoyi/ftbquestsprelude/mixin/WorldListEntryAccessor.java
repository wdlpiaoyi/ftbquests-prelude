package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the {@link LevelSummary} (and therefore the save folder id) of a world list entry. */
@Mixin(WorldSelectionList.WorldListEntry.class)
public interface WorldListEntryAccessor {

    @Accessor("summary")
    LevelSummary prelude$getSummary();
}
