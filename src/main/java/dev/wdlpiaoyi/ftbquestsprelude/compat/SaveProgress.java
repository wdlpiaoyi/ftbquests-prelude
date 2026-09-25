package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.SelectWorldScreenAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.WorldListEntryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Reads single-player save quest progress ({@code <world>/ftbquests/<team-uuid>.snbt}) so the local
 * quest book can display a save's completion state.
 */
public final class SaveProgress {

    /** A single-player save folder. */
    public record SaveInfo(String levelId, boolean hasProgress) {
    }

    /** A team stored in a save; the user picks one manually. */
    public record TeamInfo(UUID id, String name) {
    }

    private SaveProgress() {
    }

    public static Path savesDir() {
        return Minecraft.getInstance().getLevelSource().getBaseDir();
    }

    public static Path worldRoot(String levelId) {
        return savesDir().resolve(levelId);
    }

    public static boolean hasProgress(Path worldRoot) {
        Path dir = worldRoot.resolve("ftbquests");
        if (!Files.isDirectory(dir)) {
            return false;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.anyMatch(path -> path.getFileName().toString().endsWith(".snbt"));
        } catch (IOException e) {
            return false;
        }
    }

    public static List<SaveInfo> listSaves() {
        Path base = savesDir();
        if (!Files.isDirectory(base)) {
            return List.of();
        }
        List<SaveInfo> saves = new ArrayList<>();
        try (Stream<Path> stream = Files.list(base)) {
            for (Path dir : stream.filter(Files::isDirectory).sorted().toList()) {
                if (!Files.isRegularFile(dir.resolve("level.dat"))) {
                    continue;
                }
                saves.add(new SaveInfo(dir.getFileName().toString(), hasProgress(dir)));
            }
        } catch (IOException e) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not list saves in {}", base, e);
        }
        return saves;
    }

    public static List<TeamInfo> listTeams(Path worldRoot) {
        Path dir = worldRoot.resolve("ftbquests");
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        List<TeamInfo> teams = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            for (Path path : stream.filter(p -> p.getFileName().toString().endsWith(".snbt")).toList()) {
                try {
                    SNBTCompoundTag nbt = SNBT.read(path);
                    if (nbt == null) {
                        continue;
                    }
                    UUID id = readUuid(nbt, path);
                    if (id == null) {
                        continue;
                    }
                    String name = nbt.getString("name");
                    teams.add(new TeamInfo(id, name == null ? "" : name));
                } catch (Exception e) {
                    FTBQuestsPrelude.LOGGER.warn("[Prelude] Skipping unreadable team file {}", path, e);
                }
            }
        } catch (IOException e) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not list teams in {}", dir, e);
        }
        return teams;
    }

    @Nullable
    private static UUID readUuid(SNBTCompoundTag nbt, Path file) {
        try {
            String uuid = nbt.getString("uuid");
            if (uuid != null && !uuid.isEmpty()) {
                return UUID.fromString(uuid);
            }
        } catch (IllegalArgumentException ignored) {
            // fall through to the file name
        }
        String fileName = file.getFileName().toString();
        try {
            return UUID.fromString(fileName.substring(0, fileName.length() - ".snbt".length()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Resolves the world currently implied by the open screen: the highlighted entry on the
     * select-world screen, or the world being loaded on the level loading screen.
     */
    @Nullable
    public static Path selectedWorldRoot() {
        Screen screen = Minecraft.getInstance().screen;

        if (screen instanceof SelectWorldScreen selectWorld) {
            WorldSelectionList list = ((SelectWorldScreenAccessor) selectWorld).prelude$getList();
            if (list == null) {
                return null;
            }
            return list.getSelectedOpt()
                    .map(entry -> worldRoot(((WorldListEntryAccessor) (Object) entry).prelude$getSummary().getLevelId()))
                    .orElse(null);
        }

        if (screen instanceof LevelLoadingScreen) {
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                return server.getWorldPath(LevelResource.ROOT);
            }
        }

        return null;
    }
}
