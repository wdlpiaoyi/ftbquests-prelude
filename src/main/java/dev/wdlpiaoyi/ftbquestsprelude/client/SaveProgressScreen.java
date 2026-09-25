package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import dev.wdlpiaoyi.ftbquestsprelude.compat.SaveProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A small picker used by the local quest book to choose a single-player save and then a team, in
 * order to display that save's quest progress.
 */
public class SaveProgressScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 48;
    private static final int LIST_BOTTOM_MARGIN = 40;
    private static final int ROW_WIDTH = 300;

    private final boolean pickingTeam;
    private final Path worldRoot;
    private final List<SaveProgress.TeamInfo> teams;
    private final List<SaveProgress.SaveInfo> saves;
    private final Screen returnScreen;

    private int scroll;

    private SaveProgressScreen(Component title, boolean pickingTeam, Path worldRoot,
                               List<SaveProgress.TeamInfo> teams, List<SaveProgress.SaveInfo> saves,
                               Screen returnScreen) {
        super(title);
        this.pickingTeam = pickingTeam;
        this.worldRoot = worldRoot;
        this.teams = teams;
        this.saves = saves;
        this.returnScreen = returnScreen;
    }

    public static SaveProgressScreen forSaves() {
        return new SaveProgressScreen(
                Component.translatable("ftbquests_prelude.save_progress.title"),
                false, null, List.of(), SaveProgress.listSaves(), Minecraft.getInstance().screen);
    }

    public static SaveProgressScreen forTeams(Path worldRoot, List<SaveProgress.TeamInfo> teams) {
        return new SaveProgressScreen(
                Component.translatable("ftbquests_prelude.save_progress.choose_team"),
                true, worldRoot, teams, List.of(), Minecraft.getInstance().screen);
    }

    private int itemCount() {
        return pickingTeam ? teams.size() : saves.size();
    }

    private int visibleRows() {
        return Math.max(1, (height - LIST_TOP - LIST_BOTTOM_MARGIN) / ROW_HEIGHT);
    }

    @Override
    protected void init() {
        int visible = visibleRows();
        int maxScroll = Math.max(0, itemCount() - visible);
        scroll = Mth.clamp(scroll, 0, maxScroll);

        int x = (width - ROW_WIDTH) / 2;
        int end = Math.min(itemCount(), scroll + visible);

        for (int i = scroll; i < end; i++) {
            int y = LIST_TOP + (i - scroll) * ROW_HEIGHT;
            Button button = Button.builder(labelFor(i), b -> onRowClicked(indexOf(b)))
                    .bounds(x, y, ROW_WIDTH, 20)
                    .build();
            addRenderableWidget(button);
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds((width - 100) / 2, height - 30, 100, 20)
                .build());
    }

    private int indexOf(Button button) {
        // Buttons are added in order, so recover the item index from its label position.
        return scroll + Math.max(0, (button.getY() - LIST_TOP) / ROW_HEIGHT);
    }

    private Component labelFor(int index) {
        if (pickingTeam) {
            SaveProgress.TeamInfo team = teams.get(index);
            String name = team.name().isBlank() ? shortId(team.id()) : team.name() + " (" + shortId(team.id()) + ")";
            return Component.literal(name);
        }
        SaveProgress.SaveInfo save = saves.get(index);
        if (save.hasProgress()) {
            return Component.literal(save.levelId());
        }
        return Component.literal(save.levelId() + " ")
                .append(Component.translatable("ftbquests_prelude.save_progress.no_progress_suffix"));
    }

    private static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    private void onRowClicked(int index) {
        try {
            if (pickingTeam) {
                UUID teamId = teams.get(index).id();
                if (!LocalQuestSession.openSaveProgress(worldRoot, teamId)) {
                    Minecraft.getInstance().getToasts().addToast(new net.minecraft.client.gui.components.toasts.SystemToast(
                            net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                            Component.translatable("ftbquests_prelude.toast.unavailable.title"),
                            Component.translatable("ftbquests_prelude.toast.unavailable.desc")));
                }
            } else {
                SaveProgress.SaveInfo save = saves.get(index);
                Path worldRoot = SaveProgress.worldRoot(save.levelId());
                List<SaveProgress.TeamInfo> teams = SaveProgress.listTeams(worldRoot);
                if (teams.isEmpty()) {
                    Minecraft.getInstance().getToasts().addToast(new net.minecraft.client.gui.components.toasts.SystemToast(
                            net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                            Component.translatable("ftbquests_prelude.toast.no_save_progress.title"),
                            Component.translatable("ftbquests_prelude.toast.no_save_progress.desc")));
                } else if (teams.size() == 1) {
                    LocalQuestSession.openSaveProgress(worldRoot, teams.get(0).id());
                } else {
                    Minecraft.getInstance().setScreen(forTeams(worldRoot, teams));
                }
            }
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Save progress selection failed", t);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (itemCount() > visibleRows()) {
            scroll = Mth.clamp(scroll - (int) Math.signum(delta), 0, itemCount() - visibleRows());
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(returnScreen);
    }
}
