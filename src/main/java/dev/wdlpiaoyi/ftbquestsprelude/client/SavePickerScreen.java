package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.wdlpiaoyi.ftbquestsprelude.compat.SaveProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.nio.file.Path;
import java.util.List;

/**
 * FTB-style picker listing single-player saves, used by the local quest book to choose which save's
 * progress to display.
 */
public class SavePickerScreen extends AbstractButtonListScreen {

    private final Screen launcher;

    public SavePickerScreen(Screen launcher) {
        this.launcher = launcher;

        setTitle(Component.translatable("ftbquests_prelude.save_progress.title"));
        setHasSearchBox(true);
        setBorder(2, 2, 2);
        showCloseButton(true);
    }

    @Override
    public void addButtons(Panel panel) {
        for (SaveProgress.SaveInfo save : SaveProgress.listSaves()) {
            panel.add(SimpleTextButton.create(panel, describe(save), PreludeIcons.SAVE, button -> choose(save),
                    Component.literal(save.levelId())));
        }
    }

    private static Component describe(SaveProgress.SaveInfo save) {
        MutableComponent text = Component.literal(save.levelId());
        if (save.hasProgress()) {
            text.append(Component.translatable("ftbquests_prelude.save_progress.team_count", save.teamCount()));
        } else {
            text.append(Component.literal(" ")
                    .append(Component.translatable("ftbquests_prelude.save_progress.no_progress_suffix")));
        }
        return text;
    }

    private void choose(SaveProgress.SaveInfo save) {
        Path worldRoot = SaveProgress.worldRoot(save.levelId());
        List<SaveProgress.TeamInfo> teams = SaveProgress.listTeams(worldRoot);

        if (teams.isEmpty()) {
            Notifications.noSaveProgress();
        } else if (teams.size() == 1) {
            TeamPickerScreen.selectTeam(launcher, worldRoot, teams.get(0));
        } else {
            Minecraft.getInstance().setScreen(launcher);
            new TeamPickerScreen(worldRoot, teams, launcher).openGui();
        }
    }

    @Override
    protected int getTopPanelHeight() {
        return 25;
    }

    @Override
    protected void doCancel() {
        closeGui(true);
    }

    @Override
    protected void doAccept() {
        closeGui(true);
    }

    @Override
    public boolean onClosedByKey(Key key) {
        if (super.onClosedByKey(key)) {
            doCancel();
            return true;
        }
        return false;
    }
}
