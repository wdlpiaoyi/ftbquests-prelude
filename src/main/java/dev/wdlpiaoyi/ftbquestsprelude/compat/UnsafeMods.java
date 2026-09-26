package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Fail-safe for third-party mods that crash when the quest screen is rendered without a client player.
 *
 * <p>Opening the quest book outside a world means {@code Minecraft.player} is null, which some mods do
 * not expect. Such a crash cannot be fixed by redirecting: a mod that {@code @Inject}s at {@code HEAD}
 * and calls {@code cancel()} runs its own code and hides the original method body, so a
 * {@code @Redirect} placed inside that body never executes.
 *
 * <p>So for each known offender this instead turns the offender's own read-only animation switches off
 * for the duration of the local book. Those switches are early returns placed in front of the offending
 * code, and their values are only written to disk by the offender's own config screen, so changing them
 * at runtime leaves the user's config file alone. If any switch cannot be found, nothing is changed and
 * the caller is told that opening safely is impossible.
 */
public final class UnsafeMods {

    /** Mod ids with a known null-player crash in the quest screen. */
    private static final List<String> UNSAFE_MODS = List.of("certain_questing_additions");

    /** {@code certain_questing_additions} animation switches, and the code they guard. */
    private static final String CQA_CONFIG = "ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig";

    private static final List<Flag> OFFENDER_FLAGS = List.of(
            // guards ChapterPanelChapterButtonMixin#onDraw's player.getUuid()
            new Flag(CQA_CONFIG, "PANEL_BUTTON_HOVER"),
            // guards QuestButtonMixin#onDraw's player.getUuid() / isQuestPinned(player, ...)
            new Flag(CQA_CONFIG, "QUEST_HOVER")
    );

    private record Flag(String configClass, String field) {
    }

    private record AppliedFlag(Object flag, boolean previousValue) {
    }

    private static Boolean present;
    private static boolean blocked;
    private static final List<AppliedFlag> APPLIED = new ArrayList<>();

    private UnsafeMods() {
    }

    public static boolean isPresent() {
        Boolean cached = present;
        if (cached == null) {
            boolean found = false;
            for (String id : UNSAFE_MODS) {
                if (ModList.get().isLoaded(id)) {
                    found = true;
                    break;
                }
            }
            cached = found;
            present = cached;
        }
        return cached;
    }

    /** True when an unsafe mod is present and no workaround was possible. */
    public static boolean blocked() {
        return blocked;
    }

    /**
     * Makes it safe to open the local quest book without a client player.
     *
     * @return {@code false} if an unsafe mod is present and could not be worked around
     */
    public static boolean ensureSafe() {
        if (!isPresent()) {
            blocked = false;
            return true;
        }
        if (!APPLIED.isEmpty() || turnOffOffenderFlags()) {
            blocked = false;
            return true;
        }
        blocked = true;
        FTBQuestsPrelude.LOGGER.warn(
                "[Prelude] Cannot open the local quest book safely; incompatible mod(s) present: {}", UNSAFE_MODS);
        return false;
    }

    /** Restores whatever was changed, once the local book is no longer on screen. */
    public static void release() {
        if (APPLIED.isEmpty()) {
            return;
        }
        for (int i = APPLIED.size() - 1; i >= 0; i--) {
            AppliedFlag applied = APPLIED.get(i);
            try {
                setBoolean(applied.flag(), applied.previousValue());
            } catch (Throwable t) {
                FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not restore a {} switch", CQA_CONFIG, t);
            }
        }
        APPLIED.clear();
        FTBQuestsPrelude.LOGGER.info("[Prelude] Restored the quest animation switches");
    }

    /**
     * Turns every offending switch off. All or nothing: a partial change would still crash, so any
     * failure rolls back what was already changed and reports failure.
     */
    private static boolean turnOffOffenderFlags() {
        List<AppliedFlag> changed = new ArrayList<>();
        for (Flag flag : OFFENDER_FLAGS) {
            try {
                Class<?> configClass = Class.forName(flag.configClass());
                Object value = configClass.getField(flag.field()).get(null);
                if (value == null) {
                    return rollback(changed, "field " + flag.field() + " is null");
                }
                boolean previous = readBoolean(value);
                if (previous) {
                    setBoolean(value, false);
                }
                changed.add(new AppliedFlag(value, previous));
            } catch (Throwable t) {
                return rollback(changed, "cannot reach " + flag.field() + ": " + t);
            }
        }
        APPLIED.addAll(changed);
        FTBQuestsPrelude.LOGGER.info("[Prelude] Turned {} off for the duration of the local book",
                OFFENDER_FLAGS.stream().map(Flag::field).toList());
        return true;
    }

    private static boolean rollback(List<AppliedFlag> changed, String reason) {
        for (int i = changed.size() - 1; i >= 0; i--) {
            AppliedFlag applied = changed.get(i);
            try {
                setBoolean(applied.flag(), applied.previousValue());
            } catch (Throwable ignored) {
                // best effort
            }
        }
        FTBQuestsPrelude.LOGGER.warn("[Prelude] Compatibility workaround failed: {}", reason);
        return false;
    }

    private static boolean readBoolean(Object flag) throws Exception {
        Object value = flag.getClass().getMethod("get").invoke(flag);
        return value instanceof Boolean b && b;
    }

    private static void setBoolean(Object flag, boolean value) throws Exception {
        Method set = flag.getClass().getMethod("set", Object.class);
        set.invoke(flag, value);
    }
}
