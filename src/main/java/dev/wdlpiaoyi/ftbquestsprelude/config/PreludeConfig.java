package dev.wdlpiaoyi.ftbquestsprelude.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Configuration for FTB Quests: Prelude.
 *
 * <p>All values are common config, stored at {@code <config>/ftbquests_prelude-common.toml}.
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

        /** How many timestamped backups to keep. 0 disables backups. */
        public final ForgeConfigSpec.IntValue backupCount;

        /** Whether local edits are saved when the local quest screen is closed. */
        public final ForgeConfigSpec.BooleanValue autoSaveOnClose;

        /** Debounce delay before auto-saving local edits, in seconds. */
        public final ForgeConfigSpec.IntValue autoSaveDebounceSeconds;

        /** Open the local quest book in editor mode by default. */
        public final ForgeConfigSpec.BooleanValue editorModeDefault;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("FTB Quests: Prelude settings").push("general");

            backupCount = builder
                    .comment("How many timestamped backups of the quests folder to keep under <config>/ftbq_prelude/backups.",
                            "Set to 0 to disable backups.")
                    .defineInRange("backupCount", 10, 0, 1000);

            autoSaveOnClose = builder
                    .comment("Save local edits when the local quest screen is closed.")
                    .define("autoSaveOnClose", true);

            autoSaveDebounceSeconds = builder
                    .comment("Debounce delay before auto-saving local edits, in seconds.")
                    .defineInRange("autoSaveDebounceSeconds", 5, 1, 300);

            editorModeDefault = builder
                    .comment("Open the local quest book in editor mode by default.")
                    .define("editorModeDefault", false);

            builder.pop();
        }
    }
}
