package dev.wdlpiaoyi.ftbquestsprelude;

import com.mojang.logging.LogUtils;
import dev.wdlpiaoyi.ftbquestsprelude.backup.BackupManager;
import dev.wdlpiaoyi.ftbquestsprelude.compat.FTBQuestsCompat;
import dev.wdlpiaoyi.ftbquestsprelude.config.PreludeConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Main entry point for <b>FTB Quests: Prelude</b>.
 *
 * <p>An unofficial client-side addon for FTB Quests on Minecraft 1.20.1 (Forge) that lets the
 * quest book be browsed and edited outside of a world.
 */
@Mod(FTBQuestsPrelude.MOD_ID)
public class FTBQuestsPrelude {

    /** Must match the {@code mod_id} in gradle.properties and META-INF/mods.toml. */
    public static final String MOD_ID = "ftbquests_prelude";

    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBQuestsPrelude() {
        LOGGER.info("FTB Quests: Prelude loading...");

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PreludeConfig.SPEC);

        // Bootstrap that does not depend on config values or world state, so it can run
        // as early as mod construction (and is also exercised by data generation runs).
        FTBQuestsCompat.init();
        BackupManager.ensureRootDir();

        LOGGER.info("[Prelude] Ready. FTB Quests: {} (supported={}), backup dir: {}",
                FTBQuestsCompat.getVersion(),
                FTBQuestsCompat.isVersionSupported(),
                BackupManager.getRootDir());

        if (!FTBQuestsCompat.isAvailable()) {
            LOGGER.error("[Prelude] FTB Quests is missing; the local quest book will be disabled.");
        }
    }
}
