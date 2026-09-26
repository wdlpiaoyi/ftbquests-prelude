package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;

import java.util.List;

/**
 * Hides third-party buttons that only make sense against a server while the local quest book is open.
 *
 * <p>Other mods add their own buttons to FTB Quests' button panels, and those buttons drive server-side
 * work (submitting tasks, scanning storage) that cannot do anything outside a world. Rather than chase
 * individual classes, whole packages are hidden here; add a prefix when another mod turns out to be
 * server-only.
 *
 * <p>Called from the panels' {@code alignWidgets}, which runs after every {@code addWidgets} injector
 * regardless of mixin priority, so buttons added by later-priority mods are caught too.
 */
public final class ThirdPartyButtons {

    /** Package prefixes of third-party buttons to hide in the local book. */
    private static final List<String> HIDDEN_PACKAGES = List.of(
            // rs_integration: "confirm all completable checkmark tasks" and "scan storage for tasks"
            "com.huanghuang.rsintegration."
    );

    private ThirdPartyButtons() {
    }

    public static void hideFrom(Panel panel) {
        if (!LocalQuestSession.isLocalBook()) {
            return;
        }
        panel.getWidgets().removeIf(widget -> {
            String name = widget.getClass().getName();
            for (String prefix : HIDDEN_PACKAGES) {
                if (name.startsWith(prefix)) {
                    FTBQuestsPrelude.LOGGER.info("[Prelude] Hid server-only button {} in the local book", name);
                    return true;
                }
            }
            return false;
        });
    }
}
