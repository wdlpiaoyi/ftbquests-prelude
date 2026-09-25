package dev.wdlpiaoyi.ftbquestsprelude.compat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

/**
 * Availability and version gate for FTB Quests.
 *
 * <p>This class deliberately does <b>not</b> reference any FTB Quests type, so it is safe to load
 * even when FTB Quests is missing. All direct FTB Quests references must live inside this
 * {@code compat} package.
 */
public final class FTBQuestsCompat {

    public static final String FTBQUESTS_MOD_ID = "ftbquests";

    /** The FTB Quests release series that targets Minecraft 1.20.1. */
    private static final String EXPECTED_VERSION_PREFIX = "2001.";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean initialized = false;
    private static boolean available = false;
    private static String version = "unknown";
    private static boolean versionSupported = false;

    private FTBQuestsCompat() {
    }

    /** Detects FTB Quests and its version. Safe to call more than once. */
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        available = ModList.get().isLoaded(FTBQUESTS_MOD_ID);
        if (!available) {
            LOGGER.error("[Prelude] FTB Quests is not loaded; local quest book features are unavailable.");
            return;
        }

        version = ModList.get().getModContainerById(FTBQUESTS_MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
        versionSupported = version.startsWith(EXPECTED_VERSION_PREFIX);

        if (versionSupported) {
            LOGGER.info("[Prelude] Detected FTB Quests {}.", version);
        } else {
            LOGGER.warn("[Prelude] FTB Quests {} is outside the expected 1.20.1 series ({}); "
                            + "integration will run in best-effort mode.",
                    version, EXPECTED_VERSION_PREFIX + "x");
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static String getVersion() {
        return version;
    }

    public static boolean isVersionSupported() {
        return versionSupported;
    }

    /** True when the local quest book may be used: FTB Quests present and a supported version. */
    public static boolean canUseLocalQuestBook() {
        return available && versionSupported;
    }
}
