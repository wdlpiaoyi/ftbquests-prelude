package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.quests.TabButton;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import dev.wdlpiaoyi.ftbquestsprelude.compat.SaveProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * A button added to the native quest screen's bottom-right button panel that opens the save
 * progress picker, so browsing a save's progress is part of the quest book UI.
 */
public class SaveProgressButton extends TabButton {

    public SaveProgressButton(Panel panel) {
        super(panel, Component.translatable("ftbquests_prelude.save_progress.title"), PreludeIcons.SAVE);
    }

    @Override
    public void onClicked(MouseButton button) {
        playClickSound();

        Screen launcher = Minecraft.getInstance().screen;
        Path currentSave = LocalQuestSession.getCurrentSaveRoot();

        if (currentSave == null) {
            // No save context yet: start from the save picker.
            new SavePickerScreen(launcher).openGui();
            return;
        }

        List<SaveProgress.TeamInfo> teams = SaveProgress.listTeams(currentSave);
        if (teams.isEmpty()) {
            Notifications.noSaveProgress();
            return;
        }

        // Always show the picker, even when the save only has one team. Silently re-applying the team
        // that is already displayed changes nothing on screen, which reads as "the button does
        // nothing" - and it would also hide the "Switch save..." entry.
        new TeamPickerScreen(currentSave, teams, launcher).openGui();
    }
}
