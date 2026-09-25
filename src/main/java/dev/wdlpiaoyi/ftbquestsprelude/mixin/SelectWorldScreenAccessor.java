package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the world list of the select-world screen so we can read the highlighted save. */
@Mixin(SelectWorldScreen.class)
public interface SelectWorldScreenAccessor {

    @Accessor("list")
    WorldSelectionList prelude$getList();
}
