package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for the private screen state of {@link ClientQuestFile}.
 *
 * <p>Used by the local quest book session to manage the lifecycle of the native quest screen
 * without reflection. This mixin has no behaviour of its own.
 */
@Mixin(value = ClientQuestFile.class, remap = false)
public interface ClientQuestFileAccessor {

    @Accessor("questScreen")
    QuestScreen prelude$getQuestScreen();
}
