package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import net.minecraft.Util;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads the on-disk quest data at {@code <config>/ftbquests/quests} into a client-side quest file
 * and shows the native FTB Quests screen, without a server connection.
 *
 * <p>All FTB Quests types stay inside this class; callers only use the boolean API.
 */
public final class LocalQuestSession {

    /** The quest data folder that pack authors edit. */
    private static final String QUESTS_PATH = "ftbquests/quests";

    @Nullable
    private static LocalQuestSession active;

    private final ClientQuestFile file;
    private final Path questsDir;
    private QuestScreen screen;

    private LocalQuestSession(ClientQuestFile file, Path questsDir) {
        this.file = file;
        this.questsDir = questsDir;
    }

    public static Path getQuestsDir() {
        return FMLPaths.CONFIGDIR.get().resolve(QUESTS_PATH);
    }

    public static boolean isActive() {
        return active != null;
    }

    public static Path getActiveQuestsDir() {
        return active != null ? active.questsDir : getQuestsDir();
    }

    /**
     * Loads the local quest data (once) and shows the native quest screen.
     *
     * @return {@code false} if FTB Quests is unusable or there is no quest data to show
     */
    public static boolean openAndShow() {
        if (!FTBQuestsCompat.canUseLocalQuestBook()) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Cannot open local quest book: FTB Quests unavailable/unsupported.");
            return false;
        }

        Path dir = getQuestsDir();
        if (!Files.isDirectory(dir)) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] No quest data folder found at {}", dir);
            return false;
        }

        try {
            if (active == null) {
                active = new LocalQuestSession(load(dir), dir);
            }
            active.show();
            return true;
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to open the local quest book", t);
            active = null;
            return false;
        }
    }

    /** Reloads quest data from disk on the next open. */
    public static void invalidate() {
        if (active != null) {
            try {
                active.file.deleteChildren();
                active.file.deleteSelf();
            } catch (Throwable ignored) {
                // best effort
            }
        }
        active = null;
    }

    private static ClientQuestFile load(Path dir) {
        ClientQuestFile file = (ClientQuestFile) FTBQuestsClient.createClientQuestFile();
        file.readDataFull(dir);

        TeamData data = file.getOrCreateTeamData(Util.NIL_UUID);
        data.setLocked(false);
        file.selfTeamData = data;

        ClientQuestFile.INSTANCE = file;

        FTBQuestsPrelude.LOGGER.info("[Prelude] Loaded local quest data from {}", dir);
        return file;
    }

    private void show() {
        if (screen == null) {
            screen = new QuestScreen(file, null);
        }
        screen.openGui();
        screen.refreshWidgets();
    }
}
