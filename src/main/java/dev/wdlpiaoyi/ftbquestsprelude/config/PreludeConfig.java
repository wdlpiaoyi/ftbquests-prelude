package dev.wdlpiaoyi.ftbquestsprelude.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Configuration for FTB Quests: Prelude.
 *
 * <p>All values are common config, stored at {@code <config>/ftbq_prelude/ftbquests_prelude-common.toml}.
 * Because this is a client-side mod, the config only affects the local client.
 */
public final class PreludeConfig {

    public static final Common COMMON;
    public static final ForgeConfigSpec SPEC;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        SPEC = pair.getRight();
    }

    private PreludeConfig() {
    }

    public static final class Common {

        /**
         * How many timestamped backups of the quests folder to keep under
         * {@code <config>/ftbq_prelude/backups}. Older backups are deleted first.
         * Set to 0 to disable backups entirely.
         */
        public final ForgeConfigSpec.IntValue backupCount;

        /**
         * Whether pending local edits are written to disk when the local quest screen is closed.
         * Disable to only rely on the debounced auto-save.
         */
        public final ForgeConfigSpec.BooleanValue autoSaveOnClose;

        /**
         * Seconds of inactivity after an edit before it is written to disk. Lower values save
         * sooner but write more often; higher values batch more edits per write.
         */
        public final ForgeConfigSpec.IntValue autoSaveDebounceSeconds;

        /**
         * Open the local quest book directly in editor mode, so quests are unlocked/visible even
         * before any progress data exists.
         */
        public final ForgeConfigSpec.BooleanValue editorModeDefault;

        /**
         * Show the top-right "local quest book" button on the title, world selection, world
         * creation and world loading screens. The key binding still works when this is disabled.
         */
        public final ForgeConfigSpec.BooleanValue showEntryButtons;

        /**
         * Show the "local quest progress" save-icon button inside the native quest book, which
         * opens the save/team picker.
         */
        public final ForgeConfigSpec.BooleanValue showSaveProgressButton;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("FTB Quests: Prelude settings",
                    "This mod is client-side only; these values only affect the local client.").push("general");

            backupCount = builder
                    .comment("How many timestamped backups of the quests folder to keep under <config>/ftbq_prelude/backups.",
                            "Older backups are pruned after each save. Set to 0 to disable backups.")
                    .defineInRange("backupCount", 10, 0, 1000);

            autoSaveOnClose = builder
                    .comment("Save pending local edits when the local quest screen is closed.",
                            "The debounced auto-save below still runs while the screen is open.")
                    .define("autoSaveOnClose", true);

            autoSaveDebounceSeconds = builder
                    .comment("Seconds of inactivity after an edit before it is written to disk.",
                            "Lower = saves sooner but writes more often; higher = batches more edits per write.")
                    .defineInRange("autoSaveDebounceSeconds", 5, 1, 300);

            editorModeDefault = builder
                    .comment("Open the local quest book directly in editor mode.")
                    .define("editorModeDefault", false);

            showEntryButtons = builder
                    .comment("Show the top-right 'local quest book' button on the title, world selection,",
                            "world creation and world loading screens. The key binding still works when disabled.")
                    .define("showEntryButtons", true);

            showSaveProgressButton = builder
                    .comment("Show the 'local quest progress' save-icon button inside the native quest book.")
                    .define("showSaveProgressButton", true);

            builder.pop();
        }
    }
}
