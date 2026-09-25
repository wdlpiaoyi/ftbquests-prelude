package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import dev.wdlpiaoyi.ftbquestsprelude.compat.SaveProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * FTB-style picker listing the teams of a save. Left-click selects a team; right-click opens a
 * context menu with the team's members.
 */
public class TeamPickerScreen extends AbstractButtonListScreen {

    /** FTB Teams' own "my team" icon, as used by its sidebar button. */
    private static final Icon TEAM_ICON = Icon.getIcon("ftbteams:textures/teams.png");

    private final Path worldRoot;
    private final List<SaveProgress.TeamInfo> teams;
    private final Screen launcher;

    public TeamPickerScreen(Path worldRoot, List<SaveProgress.TeamInfo> teams, Screen launcher) {
        this.worldRoot = worldRoot;
        this.teams = teams;
        this.launcher = launcher;

        setTitle(Component.translatable("ftbquests_prelude.save_progress.choose_team"));
        setHasSearchBox(true);
        setBorder(2, 2, 2);
        showCloseButton(true);
    }

    @Override
    public void addButtons(Panel panel) {
        panel.add(SimpleTextButton.create(panel,
                Component.translatable("ftbquests_prelude.save_progress.switch_save"), FloppyIcon.INSTANCE,
                button -> {
                    Minecraft.getInstance().setScreen(launcher);
                    new SavePickerScreen(launcher).openGui();
                }));

        for (SaveProgress.TeamInfo team : teams) {
            panel.add(new TeamButton(panel, team));
        }
    }

    /** Applies the chosen team's progress and returns to the launcher, refreshing the quest book. */
    public static void selectTeam(Screen launcher, Path worldRoot, SaveProgress.TeamInfo team) {
        LocalQuestSession.applySaveProgress(worldRoot, team.id());

        Minecraft.getInstance().setScreen(launcher);
        QuestScreen questScreen = ClientUtils.getCurrentGuiAs(QuestScreen.class);
        if (questScreen != null) {
            questScreen.refreshWidgets();
        } else {
            LocalQuestSession.openBook();
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

    private class TeamButton extends SimpleTextButton {

        private final SaveProgress.TeamInfo team;

        TeamButton(Panel panel, SaveProgress.TeamInfo team) {
            super(panel, Component.literal(team.name()), TEAM_ICON);
            this.team = team;
            setHeight(16);
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();

            if (button.isRight()) {
                List<ContextMenuItem> members = new ArrayList<>();
                for (String member : team.memberNames()) {
                    members.add(new ContextMenuItem(Component.literal(member), Icons.PLAYER, b -> {
                    }));
                }
                getGui().openContextMenu(members);
            } else {
                selectTeam(launcher, worldRoot, team);
            }
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            list.add(Component.translatable("ftbquests_prelude.save_progress.member_count", team.memberCount()));
            for (String member : team.memberNames()) {
                list.add(Component.literal(" - " + member));
            }
        }
    }
}
