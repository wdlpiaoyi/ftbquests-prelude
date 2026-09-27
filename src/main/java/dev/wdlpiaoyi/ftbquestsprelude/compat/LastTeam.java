package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

/**
 * Remembers which team was last used for each single-player save, so re-opening the book for a save you
 * have already visited goes straight to that team instead of asking again.
 *
 * <p>Stored next to this mod's config as a plain properties file, so a broken or hand-edited file can
 * never break anything.
 */
public final class LastTeam {

    private static final String FILE_NAME = "last_team.properties";

    @Nullable
    private static Properties cache;

    private LastTeam() {
    }

    @Nullable
    public static UUID get(String levelId) {
        try {
            String value = load().getProperty(levelId);
            return value == null || value.isEmpty() ? null : UUID.fromString(value);
        } catch (Throwable t) {
            return null;
        }
    }

    public static void put(String levelId, UUID teamId) {
        try {
            Properties properties = load();
            properties.setProperty(levelId, teamId.toString());
            Path file = file();
            Files.createDirectories(file.getParent());
            try (var out = Files.newOutputStream(file)) {
                properties.store(out, "Last team used per save (managed by FTB Quests: Prelude)");
            }
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not remember the team for save {}", levelId, t);
        }
    }

    private static Properties load() throws IOException {
        Properties properties = cache;
        if (properties == null) {
            properties = new Properties();
            Path file = file();
            if (Files.isRegularFile(file)) {
                try (var in = Files.newInputStream(file)) {
                    properties.load(in);
                }
            }
            cache = properties;
        }
        return properties;
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("ftbq_prelude").resolve(FILE_NAME);
    }
}
