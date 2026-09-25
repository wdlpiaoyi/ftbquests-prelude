package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.architectury.networking.simple.BaseC2SMessage;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.ChangeChapterGroupMessage;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftbquests.net.CreateTaskAtMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectMessage;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.net.MoveChapterGroupMessage;
import dev.ftb.mods.ftbquests.net.MoveChapterMessage;
import dev.ftb.mods.ftbquests.net.MoveMovableMessage;
import dev.ftb.mods.ftbquests.net.ReorderItemMessage;
import dev.ftb.mods.ftbquests.net.ToggleEditingModeMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.ChangeChapterGroupMessageAccessor;
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
 */
public final class LocalEditBridge {

    private LocalEditBridge() {
    }

    public static void handle(BaseC2SMessage message) {
        try {
            if (message instanceof ToggleEditingModeMessage) {
                LocalQuestSession.toggleEditing();
                refreshScreen();
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
            } else if (message instanceof ReorderItemMessage m) {
                reorderItem((ReorderItemMessageAccessor) m);
            } else {
                // Not an edit we handle locally (claims, progress, copy, ...) - ignore it.
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
        QuestScreen screen = ClientUtils.getCurrentGuiAs(QuestScreen.class);
        if (screen != null) {
            screen.refreshWidgets();
        }
    }
}
