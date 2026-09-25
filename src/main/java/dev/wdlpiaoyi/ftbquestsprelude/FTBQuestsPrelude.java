package dev.wdlpiaoyi.ftbquestsprelude;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Main entry point for <b>FTB Quests: Prelude</b>.
 *
 * <p>An unofficial addon for FTB Quests on Minecraft 1.20.1 (Forge).
 */
@Mod(FTBQuestsPrelude.MOD_ID)
public class FTBQuestsPrelude {

    /** Must match the {@code mod_id} in gradle.properties and META-INF/mods.toml. */
    public static final String MOD_ID = "ftbquests_prelude";

    private static final Logger LOGGER = LogUtils.getLogger();

    public FTBQuestsPrelude() {
        LOGGER.info("FTB Quests: Prelude loading...");
        // TODO: hook into the FTB Quests API / event bus here.
    }
}
