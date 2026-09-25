package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.backup.BackupManager;
import dev.wdlpiaoyi.ftbquestsprelude.config.PreludeConfig;
import net.minecraft.Util;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads the on-disk quest data at {@code <config>/ftbquests/quests} into a client-side quest file
 * and shows the native FTB Quests screen, without a server connection.
 *
 * <p>In editor mode, edits made through the native GUI are applied locally (see
 * {@link LocalEditBridge}) and persisted back to disk with an automatic backup.
 *
 * <p>All FTB Quests types stay inside this class; callers only use the boolean API.
 */
public final class LocalQuestSession {

    /** The quest data folder that pack authors edit. */
    private static final String QUESTS_PATH = "ftbquests/quests";

    @Nullable
    private static LocalQuestSession active;

    /** Client tick counter, used for save debouncing. */
    private static long tickCounter;

    private final ClientQuestFile file;
    private final Path questsDir;

    private boolean editing;
    private boolean dirty;
    private long lastEditTick;

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

    public static boolean isEditing() {
        return active != null && active.editing;
    }

    public static boolean isEditing(ClientQuestFile file) {
        return active != null && active.file == file && active.editing;
    }

    public static void toggleEditing() {
        if (active != null) {
            active.editing = !active.editing;
            FTBQuestsPrelude.LOGGER.info("[Prelude] Local editor mode {}", active.editing ? "enabled" : "disabled");
            if (!active.editing) {
                active.saveIfDirty();
            }
        }
    }

    /** Marks the local quest data as changed; the save is debounced by {@link #tick()}. */
    public static void markDirty() {
        if (active != null) {
            active.dirty = true;
            active.lastEditTick = tickCounter;
        }
    }

    /** Called once per client tick; flushes pending local edits after the configured debounce. */
    public static void tick() {
        tickCounter++;
        LocalQuestSession session = active;
        if (session == null || !session.dirty) {
            return;
        }

        // Save right away once the quest book is no longer the active screen (e.g. after ESC).
        boolean screenOpen = ClientUtils.getCurrentGuiAs(QuestScreen.class) != null;
        if (!screenOpen && PreludeConfig.COMMON.autoSaveOnClose.get()) {
            session.save();
            return;
        }

        long debounceTicks = (long) PreludeConfig.COMMON.autoSaveDebounceSeconds.get() * 20L;
        if (tickCounter - session.lastEditTick >= debounceTicks) {
            session.save();
        }
    }

    /**
     * Loads the local quest data (reloading it if a world session invalidated it) and shows the
     * native quest screen.
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
            // A real server session replaces the client quest file (and invalidates ours), so
            // reload from disk whenever our cached instance is no longer the active one.
            if (active != null && (ClientQuestFile.INSTANCE != active.file || !ClientQuestFile.exists())) {
                FTBQuestsPrelude.LOGGER.info("[Prelude] Local quest session is stale, reloading from disk.");
                active = null;
            }

            if (active == null) {
                active = new LocalQuestSession(load(dir), dir);
                active.editing = PreludeConfig.COMMON.editorModeDefault.get();
            }
            active.show();
            return true;
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to open the local quest book", t);
            active = null;
            return false;
        }
    }

    /** Saves pending changes and discards the local quest data. */
    public static void invalidate() {
        if (active != null) {
            active.saveIfDirty();
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

        // Allows FTB Quests' own "edit mode" button to show up; canEdit() itself is handled by a mixin.
        file.setEditorPermission(true);

        ClientQuestFile.INSTANCE = file;

        FTBQuestsPrelude.LOGGER.info("[Prelude] Loaded local quest data from {}", dir);
        return file;
    }

    /**
     * Creates a fresh screen each time. This is important: FTB Library captures the currently
     * displayed screen as the screen's {@code prevScreen} in its constructor, so reusing a cached
     * instance would always return to whichever screen was open the first time.
     */
    private void show() {
        QuestScreen questScreen = new QuestScreen(file, null);
        questScreen.openGui();
        questScreen.refreshWidgets();
    }

    private void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    private void save() {
        try {
            BackupManager.backupDirectory(questsDir, "quests");
            file.writeDataFull(questsDir);
            BackupManager.prune(PreludeConfig.COMMON.backupCount.get());
            FTBQuestsPrelude.LOGGER.info("[Prelude] Saved local quest data to {}", questsDir);
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to save local quest data", t);
        }
        dirty = false;
    }
}
