package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.architectury.networking.simple.BaseC2SMessage;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.ChangeChapterGroupMessage;
import dev.ftb.mods.ftbquests.net.CopyChapterImageMessage;
import dev.ftb.mods.ftbquests.net.CopyQuestMessage;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.net.CreateTaskAtMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.ForceSaveMessage;
import dev.ftb.mods.ftbquests.net.MoveChapterGroupMessage;
import dev.ftb.mods.ftbquests.net.MoveChapterMessage;
import dev.ftb.mods.ftbquests.net.MoveMovableMessage;
import dev.ftb.mods.ftbquests.net.ReorderItemMessage;
import dev.ftb.mods.ftbquests.net.ToggleEditingModeMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.ChangeChapterGroupMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.CopyChapterImageMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.CopyQuestMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.CreateObjectMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.CreateTaskAtMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.DeleteObjectMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.EditObjectMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.MoveChapterGroupMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.MoveChapterMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.MoveMovableMessageAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.ReorderItemMessageAccessor;
import net.minecraft.Util;

/**
 * Applies FTB Quests client-to-server edit messages to the local quest file instead of the server.
 *
 * <p>Outside a world the native quest GUI still emits edit messages when in editor mode. Since there
 * is no server to receive them, we mirror the server-side effect locally (reusing FTB Quests' own
 * client-side appliers where possible) and let {@link LocalQuestSession} persist the result.
 *
 * <p>Messages that represent server-authoritative state rather than editing (reward claiming, task
 * submission, progress resets, per-player pinning, ...) are intentionally ignored - they would need a
 * server to be meaningful.
 */
public final class LocalEditBridge {

    /** Set from the client layer so this class does not have to depend on the UI package. */
    private static Runnable saveNotifier = () -> {
    };

    /** Set when an edit should refresh the open quest screen on the next client tick. */
    private static boolean refreshPending;

    private LocalEditBridge() {
    }

    /** Registers the callback that reports a successful on-demand save to the player. */
    public static void setSaveNotifier(Runnable notifier) {
        saveNotifier = notifier;
    }

    public static void handle(BaseC2SMessage message) {
        try {
            if (message instanceof ToggleEditingModeMessage) {
                LocalQuestSession.toggleEditing();
                refreshScreen();
                return;
            }

            // The book's own "save" button: flush to disk right away.
            if (message instanceof ForceSaveMessage) {
                if (LocalQuestSession.forceSave()) {
                    saveNotifier.run();
                }
                return;
            }

            if (!LocalQuestSession.isEditing()) {
                return;
            }

            if (message instanceof EditObjectMessage m) {
                EditObjectMessageAccessor a = (EditObjectMessageAccessor) m;
                FTBQuestsNetClient.editObject(a.prelude$getId(), a.prelude$getNbt());
            } else if (message instanceof DeleteObjectMessage m) {
                FTBQuestsNetClient.deleteObject(((DeleteObjectMessageAccessor) m).prelude$getId());
            } else if (message instanceof MoveMovableMessage m) {
                MoveMovableMessageAccessor a = (MoveMovableMessageAccessor) m;
                FTBQuestsNetClient.moveQuest(a.prelude$getId(), a.prelude$getChapterId(), a.prelude$getX(), a.prelude$getY());
            } else if (message instanceof MoveChapterMessage m) {
                MoveChapterMessageAccessor a = (MoveChapterMessageAccessor) m;
                FTBQuestsNetClient.moveChapter(a.prelude$getId(), a.prelude$getMovingUp());
            } else if (message instanceof ChangeChapterGroupMessage m) {
                ChangeChapterGroupMessageAccessor a = (ChangeChapterGroupMessageAccessor) m;
                FTBQuestsNetClient.changeChapterGroup(a.prelude$getChapterId(), a.prelude$getGroupId());
            } else if (message instanceof MoveChapterGroupMessage m) {
                MoveChapterGroupMessageAccessor a = (MoveChapterGroupMessageAccessor) m;
                FTBQuestsNetClient.moveChapterGroup(a.prelude$getId(), a.prelude$getMovingUp());
            } else if (message instanceof CreateObjectMessage m) {
                CreateObjectMessageAccessor a = (CreateObjectMessageAccessor) m;
                ClientQuestFile file = ClientQuestFile.INSTANCE;
                FTBQuestsNetClient.createObject(file.newID(), a.prelude$getParent(), a.prelude$getType(),
                        a.prelude$getNbt(), a.prelude$getExtra(), Util.NIL_UUID);
            } else if (message instanceof CreateTaskAtMessage m) {
                createTaskAt((CreateTaskAtMessageAccessor) m);
            } else if (message instanceof CopyQuestMessage m) {
                copyQuest((CopyQuestMessageAccessor) m);
            } else if (message instanceof CopyChapterImageMessage m) {
                copyChapterImage((CopyChapterImageMessageAccessor) m);
            } else if (message instanceof ReorderItemMessage m) {
                reorderItem((ReorderItemMessageAccessor) m);
            } else {
                // Not an edit we handle locally (claims, progress, pinning, ...) - ignore it.
                return;
            }

            LocalQuestSession.markDirty();
            refreshScreen();
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to apply local edit {}",
                    message.getClass().getSimpleName(), t);
        }
    }

    /** Mirrors the server-side handling of {@code CreateTaskAtMessage} (creates a quest and a task). */
    private static void createTaskAt(CreateTaskAtMessageAccessor a) {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        Chapter chapter = file.getChapter(a.prelude$getChapterId());
        if (chapter == null) {
            return;
        }

        Quest quest = new Quest(file.newID(), chapter);
        quest.setX(a.prelude$getX());
        quest.setY(a.prelude$getY());
        quest.onCreated();

        Task task = a.prelude$getType().createTask(file.newID(), quest);
        task.readData(a.prelude$getNbt());
        task.onCreated();

        file.refreshIDMap();
        file.clearCachedData();
    }

    /**
     * Mirrors the server-side handling of {@code CopyQuestMessage}: deep-copies the quest (and,
     * unless the caller asked otherwise, its dependencies, tasks and rewards) into the chapter.
     */
    private static void copyQuest(CopyQuestMessageAccessor a) {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (!(file.get(a.prelude$getQuestId()) instanceof Quest toCopy)) {
            return;
        }
        if (!(file.get(a.prelude$getChapterId()) instanceof Chapter chapter)) {
            return;
        }

        Quest newQuest = QuestObjectBase.copy(toCopy, () -> new Quest(file.newID(), chapter));
        if (newQuest == null) {
            return;
        }
        if (!a.prelude$copyDeps()) {
            newQuest.clearDependencies();
        }
        newQuest.setX(a.prelude$getX());
        newQuest.setY(a.prelude$getY());
        newQuest.onCreated();

        for (Task task : toCopy.getTasks()) {
            Task newTask = QuestObjectBase.copy(task,
                    () -> TaskType.createTask(file.newID(), newQuest, task.getType().getTypeForNBT()));
            if (newTask != null) {
                newTask.onCreated();
            }
        }
        for (Reward reward : toCopy.getRewards()) {
            Reward newReward = QuestObjectBase.copy(reward,
                    () -> RewardType.createReward(file.newID(), newQuest, reward.getType().getTypeForNBT()));
            if (newReward != null) {
                newReward.onCreated();
            }
        }

        file.refreshIDMap();
        file.clearCachedData();
    }

    /** Mirrors the server-side handling of {@code CopyChapterImageMessage}. */
    private static void copyChapterImage(CopyChapterImageMessageAccessor a) {
        ChapterImage image = a.prelude$getImage();
        if (image == null) {
            return;
        }
        Chapter chapter = image.getChapter();
        if (chapter == null) {
            chapter = ClientQuestFile.INSTANCE.getChapter(a.prelude$getChapterId());
        }
        if (chapter != null) {
            chapter.addImage(image);
        }
    }

    /** Mirrors the server-side handling of {@code ReorderItemMessage}. */
    private static void reorderItem(ReorderItemMessageAccessor a) {
        QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(a.prelude$getId());
        if (object instanceof Task task) {
            if (a.prelude$getMoveRight()) {
                task.getQuest().moveTaskRight(task);
            } else {
                task.getQuest().moveTaskLeft(task);
            }
        } else if (object instanceof Reward reward) {
            if (a.prelude$getMoveRight()) {
                reward.getQuest().moveRewardRight(reward);
            } else {
                reward.getQuest().moveRewardLeft(reward);
            }
        }
    }

    private static void refreshScreen() {
        refreshNow();
        // Repeat on the next tick: a freshly created object may only be attached to its parent after
        // this call, in which case the immediate refresh happened too early.
        refreshPending = true;
    }

    /** Applies a refresh queued by {@link #refreshScreen()}; called once per client tick. */
    public static void tick() {
        if (!refreshPending) {
            return;
        }
        refreshPending = false;
        refreshNow();
    }

    private static void refreshNow() {
        QuestScreen screen = ClientUtils.getCurrentGuiAs(QuestScreen.class);
        if (screen == null) {
            return;
        }
        screen.refreshWidgets();
        // FTB Quests refreshes the quest detail panel through this; without it a newly added task or
        // reward only shows up after re-opening the quest.
        screen.refreshViewQuestPanel();

        Quest viewed = screen.getViewedQuest();
        if (viewed != null) {
            FTBQuestsPrelude.LOGGER.info("[Prelude] Refreshed quest view {}: tasks={}, rewards={}",
                    viewed.id, viewed.getTasks().size(), viewed.getRewards().size());
        }
    }
}
