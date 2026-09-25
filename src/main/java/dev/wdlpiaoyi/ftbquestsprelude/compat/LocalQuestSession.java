package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.backup.BackupManager;
import dev.wdlpiaoyi.ftbquestsprelude.config.PreludeConfig;
import net.minecraft.Util;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Loads the on-disk quest data at {@code <config>/ftbquests/quests} into a client-side quest file
 * and shows the native FTB Quests screen, without a server connection.
 *
 * <p>In editor mode, edits made through the native GUI are applied locally (see
 * {@link LocalEditBridge}) and persisted back to disk with an automatic backup.
 *
 * <p>It can also display a single-player save's quest progress by loading that save's
 * {@link TeamData} (see {@link SaveProgress}).
 *
 * <p>All FTB Quests types stay inside this class; callers only use the boolean API.
 */
public final class LocalQuestSession {

    /** The quest data folder that pack authors edit. */
    private static final String QUESTS_PATH = "ftbquests/quests";

    @Nullable
    private static LocalQuestSession active;

    /** Save whose progress is currently shown, or null for the plain local book. */
    @Nullable
    private static Path currentSaveRoot;

    /** Client tick counter, used for save debouncing. */
    private static long tickCounter;

    private final ClientQuestFile file;
    private final Path questsDir;

    private boolean editing;
    private boolean dirty;
    private long lastEditTick;
    private long fingerprint;

    @Nullable
    private TeamData defaultTeamData;

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

    /** The save whose progress is currently shown, or null for the plain local book. */
    @Nullable
    public static Path getCurrentSaveRoot() {
        return currentSaveRoot;
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
     * Loads the local quest data and shows the native quest screen (no player progress).
     *
     * @return {@code false} if FTB Quests is unusable or there is no quest data to show
     */
    public static boolean openAndShow() {
        if (!prepareSession()) {
            return false;
        }
        currentSaveRoot = null;
        try {
            active.file.selfTeamData = active.defaultTeamData;
            active.show();
            return true;
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to open the local quest book", t);
            disposeActive();
            return false;
        }
    }

    /** Opens (or reopens) the local quest book without changing the currently shown team. */
    public static boolean openBook() {
        if (!prepareSession()) {
            return false;
        }
        try {
            active.show();
            return true;
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to open the local quest book", t);
            disposeActive();
            return false;
        }
    }

    /** Applies a save's team progress without opening a screen (the caller refreshes the book). */
    public static boolean applySaveProgress(Path worldRoot, UUID teamId) {
        if (!prepareSession()) {
            return false;
        }
        try {
            active.applyTeam(worldRoot, teamId);
            currentSaveRoot = worldRoot;
            return true;
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to apply save quest progress", t);
            return false;
        }
    }

    /**
     * Loads the local quest data, applies the given single-player save's team progress and shows the
     * native quest screen.
     *
     * @return {@code false} if FTB Quests is unusable or there is no quest data to show
     */
    public static boolean openSaveProgress(Path worldRoot, UUID teamId) {
        if (!prepareSession()) {
            return false;
        }
        try {
            active.applyTeam(worldRoot, teamId);
            currentSaveRoot = worldRoot;
            active.show();
            return true;
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to open save quest progress", t);
            disposeActive();
            return false;
        }
    }

    /** Saves pending changes and discards the local quest data. */
    public static void invalidate() {
        if (active != null) {
            active.saveIfDirty();
        }
        disposeActive();
        currentSaveRoot = null;
    }

    private static boolean prepareSession() {
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
            ensureSession(dir);
            return true;
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to load the local quest book", t);
            disposeActive();
            return false;
        }
    }

    /** (Re)loads the session if it is missing or the quest files changed on disk. */
    private static void ensureSession(Path dir) {
        long diskFingerprint = computeFingerprint(dir);
        boolean needReload = active == null
                || ClientQuestFile.INSTANCE != active.file
                || !ClientQuestFile.exists()
                || active.fingerprint != diskFingerprint;

        if (needReload) {
            reload(dir, diskFingerprint);
        }
    }

    private static void reload(Path dir, long diskFingerprint) {
        boolean wasEditing = active != null && active.editing;
        disposeActive();

        LocalQuestSession session = new LocalQuestSession(load(dir), dir);
        session.editing = wasEditing;
        session.fingerprint = diskFingerprint;
        session.defaultTeamData = session.file.selfTeamData;
        active = session;
        FTBQuestsPrelude.LOGGER.info("[Prelude] Loaded local quest data from {}", dir);
    }

    private static void disposeActive() {
        if (active != null) {
            try {
                active.file.deleteChildren();
                active.file.deleteSelf();
            } catch (Throwable ignored) {
                // best effort
            }
            active = null;
        }
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
        return file;
    }

    /** Loads a save's {@link TeamData} and makes it the team shown by the quest screen. */
    private void applyTeam(Path worldRoot, UUID teamId) {
        TeamData data = file.getOrCreateTeamData(teamId);

        Path teamFile = worldRoot.resolve("ftbquests").resolve(teamId + ".snbt");
        if (Files.isRegularFile(teamFile)) {
            try {
                SNBTCompoundTag nbt = SNBT.read(teamFile);
                if (nbt != null) {
                    data.deserializeNBT(nbt);
                }
            } catch (Throwable t) {
                FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not read team data from {}", teamFile, t);
            }
        }

        file.selfTeamData = data;
        FTBQuestsPrelude.LOGGER.info("[Prelude] Showing save progress for team {}", teamId);
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
            pruneOrphanFiles();
            fingerprint = computeFingerprint(questsDir);
            BackupManager.prune(PreludeConfig.COMMON.backupCount.get());
            FTBQuestsPrelude.LOGGER.info("[Prelude] Saved local quest data to {}", questsDir);
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to save local quest data", t);
        }
        dirty = false;
    }

    /**
     * FTB Quests writes chapter/reward-table files but never removes the files of renamed or
     * deleted objects, which leaves stale duplicates behind. Delete any {@code .snbt} file that we
     * did not just write.
     */
    private void pruneOrphanFiles() {
        Set<String> chapters = new HashSet<>();
        file.getAllChapters().forEach(chapter -> chapters.add(chapter.getFilename() + ".snbt"));
        pruneDir(questsDir.resolve("chapters"), chapters);

        Set<String> tables = new HashSet<>();
        file.getRewardTables().forEach(table -> tables.add(table.getFilename() + ".snbt"));
        pruneDir(questsDir.resolve("reward_tables"), tables);
    }

    private static void pruneDir(Path dir, Set<String> keep) {
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> orphans = stream
                    .filter(path -> path.getFileName().toString().endsWith(".snbt"))
                    .filter(path -> !keep.contains(path.getFileName().toString()))
                    .toList();
            for (Path orphan : orphans) {
                try {
                    Files.deleteIfExists(orphan);
                    FTBQuestsPrelude.LOGGER.info("[Prelude] Removed orphan quest file {}", orphan.getFileName());
                } catch (IOException e) {
                    FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not remove orphan quest file {}", orphan, e);
                }
            }
        } catch (IOException e) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not scan {}", dir, e);
        }
    }

    /** Cheap change detector over the quest data directory (file names, sizes and timestamps). */
    private static long computeFingerprint(Path dir) {
        if (!Files.isDirectory(dir)) {
            return -1L;
        }
        long hash = 0L;
        try (Stream<Path> stream = Files.walk(dir)) {
            List<Path> files = stream.filter(Files::isRegularFile).sorted().toList();
            for (Path path : files) {
                hash = hash * 31L + dir.relativize(path).toString().hashCode();
                hash = hash * 31L + Files.size(path);
                hash = hash * 31L + Files.getLastModifiedTime(path).toMillis();
            }
        } catch (IOException e) {
            return -2L;
        }
        return hash;
    }
}
