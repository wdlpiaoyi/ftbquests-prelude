package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import dev.wdlpiaoyi.ftbquestsprelude.config.PreludeConfig;

import java.util.List;

/**
 * Hides third-party buttons that only make sense against a server while the local quest book is open.
 *
 * <p>Other mods add their own buttons to FTB Quests' button panels, and those buttons usually drive
 * server-side work (submitting tasks, scanning storage) that cannot do anything outside a world. The
 * package prefixes are configured in {@code hiddenButtonPackages}, so no release is needed to hide a
 * newly found one.
 *
 * <p>Called from the panels' {@code alignWidgets}, which runs after every {@code addWidgets} injector
 * regardless of mixin priority, so buttons added by later-priority mods are caught too.
 */
public final class ThirdPartyButtons {

    private ThirdPartyButtons() {
    }

    public static void hideFrom(Panel panel) {
        if (!LocalQuestSession.isLocalBook()) {
            return;
        }

        List<? extends String> prefixes;
        try {
            prefixes = PreludeConfig.COMMON.hiddenButtonPackages.get();
        } catch (Throwable t) {
            return; // config not available yet
        }
        if (prefixes.isEmpty()) {
            return;
        }

        panel.getWidgets().removeIf(widget -> matches(widget.getClass().getName(), prefixes));
    }

    private static boolean matches(String className, List<? extends String> prefixes) {
        for (String prefix : prefixes) {
            if (prefix != null && !prefix.isEmpty() && className.startsWith(prefix)) {
                FTBQuestsPrelude.LOGGER.info("[Prelude] Hid server-only button {} in the local book", className);
                return true;
            }
        }
        return false;
    }
}
