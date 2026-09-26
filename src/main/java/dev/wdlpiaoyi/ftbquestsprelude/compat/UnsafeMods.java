package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Fail-safe for third-party mods that crash when the quest screen is rendered without a client player.
 *
 * <p>Opening the quest book outside a world means {@code Minecraft.player} is null, which some mods do
 * not expect. Such a crash cannot be fixed by redirecting: a mod that {@code @Inject}s at {@code HEAD}
 * and calls {@code cancel()} runs its own code and hides the original method body, so a
 * {@code @Redirect} placed inside that body never executes.
 *
 * <p>So for each known offender this instead neutralises the offender's own switch for the duration of
 * the local book, or - if that is not possible - reports that the book cannot be opened safely. The
 * switches are read-only flags whose value is only written to disk by the offender's own config screen,
 * so changing them at runtime leaves the user's config file alone.
 */
public final class UnsafeMods {

    /** Mod ids with a known null-player crash in the quest screen. */
    private static final List<String> UNSAFE_MODS = List.of("certain_questing_additions");

    /**
     * {@code certain_questing_additions}' chapter-button mixin returns early when this flag is false,
     * before the call that dereferences the null player.
     */
    private static final String CQA_CONFIG = "ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig";
    private static final String CQA_HOVER_FLAG = "PANEL_BUTTON_HOVER";

    private static Boolean present;
    private static boolean blocked;

    /** The flag object that was turned off, and what it was before. */
    private static Object disabledFlag;
    private static boolean previousFlagValue;

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
        if (turnOffOffenderFlag()) {
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
        Object flag = disabledFlag;
        if (flag == null) {
            return;
        }
        disabledFlag = null;
        try {
            setBoolean(flag, previousFlagValue);
            FTBQuestsPrelude.LOGGER.info("[Prelude] Restored {} to {}", CQA_HOVER_FLAG, previousFlagValue);
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not restore {}", CQA_HOVER_FLAG, t);
        }
    }

    private static boolean turnOffOffenderFlag() {
        if (disabledFlag != null) {
            return true; // already applied
        }
        try {
            Class<?> configClass = Class.forName(CQA_CONFIG);
            Object flag = configClass.getField(CQA_HOVER_FLAG).get(null);
            if (flag == null) {
                return false;
            }
            boolean current = readBoolean(flag);
            if (current) {
                setBoolean(flag, false);
            }
            previousFlagValue = current;
            disabledFlag = flag;
            FTBQuestsPrelude.LOGGER.info("[Prelude] Turned {} off for the duration of the local book", CQA_HOVER_FLAG);
            return true;
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Compatibility workaround for {} failed", CQA_CONFIG, t);
            return false;
        }
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
