package dev.wdlpiaoyi.ftbquestsprelude.compat;

import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.SelectWorldScreenAccessor;
import dev.wdlpiaoyi.ftbquestsprelude.mixin.WorldListEntryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
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
 * Reads single-player save quest progress ({@code <world>/ftbquests/<team-uuid>.snbt}) and FTB Teams
 * metadata ({@code <world>/ftbteams/...}) so the local quest book can display a save's completion
 * state.
 */
public final class SaveProgress {

    /** A single-player save folder. */
    public record SaveInfo(String levelId, int teamCount, boolean hasProgress) {
    }

    /** A team stored in a save. */
    public record TeamInfo(UUID id, String name, List<String> memberNames) {
        public int memberCount() {
            return memberNames.size();
        }
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
        return !listTeamIds(worldRoot).isEmpty();
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
                int teamCount = listTeamIds(dir).size();
                saves.add(new SaveInfo(dir.getFileName().toString(), teamCount, teamCount > 0));
            }
        } catch (IOException e) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not list saves in {}", base, e);
        }
        return saves;
    }

    public static List<TeamInfo> listTeams(Path worldRoot) {
        List<TeamInfo> teams = new ArrayList<>();
        for (UUID id : listTeamIds(worldRoot)) {
            teams.add(readTeam(worldRoot, id));
        }
        return teams;
    }

    private static List<UUID> listTeamIds(Path worldRoot) {
        Path dir = worldRoot.resolve("ftbquests");
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        List<UUID> ids = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            for (Path path : stream.filter(p -> p.getFileName().toString().endsWith(".snbt")).toList()) {
                UUID id = readUuid(path);
                if (id != null) {
                    ids.add(id);
                }
            }
        } catch (IOException e) {
            FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not list teams in {}", dir, e);
        }
        return ids;
    }

    /** Reads a team's display name and member names from the save's FTB Teams data. */
    private static TeamInfo readTeam(Path worldRoot, UUID teamId) {
        String name = "";
        List<String> memberNames = new ArrayList<>();

        SNBTCompoundTag team = readTeamsFile(worldRoot, teamId);
        if (team != null) {
            SNBTCompoundTag properties = team.getCompound("properties");
            String displayName = properties.getString("ftbteams:display_name");
            if (displayName != null && !displayName.isEmpty()) {
                name = displayName;
            }

            if ("player".equals(team.getString("type"))) {
                String playerName = team.getString("player_name");
                memberNames.add((playerName == null || playerName.isEmpty()) ? shortId(teamId) : playerName);
            } else {
                for (String key : team.getCompound("ranks").getAllKeys()) {
                    memberNames.add(resolvePlayerName(worldRoot, key));
                }
            }
        }

        if (name.isEmpty()) {
            name = "Team " + shortId(teamId);
        }
        if (memberNames.isEmpty()) {
            memberNames.add(shortId(teamId));
        }
        return new TeamInfo(teamId, name, memberNames);
    }

    @Nullable
    private static SNBTCompoundTag readTeamsFile(Path worldRoot, UUID teamId) {
        Path base = worldRoot.resolve("ftbteams");
        for (String sub : new String[]{"party", "player", "server"}) {
            Path path = base.resolve(sub).resolve(teamId + ".snbt");
            if (Files.isRegularFile(path)) {
                try {
                    return SNBT.read(path);
                } catch (Exception e) {
                    FTBQuestsPrelude.LOGGER.warn("[Prelude] Could not read team file {}", path, e);
                }
            }
        }
        return null;
    }

    private static String resolvePlayerName(Path worldRoot, String playerUuid) {
        Path path = worldRoot.resolve("ftbteams").resolve("player").resolve(playerUuid + ".snbt");
        if (Files.isRegularFile(path)) {
            try {
                SNBTCompoundTag nbt = SNBT.read(path);
                if (nbt != null) {
                    String playerName = nbt.getString("player_name");
                    if (playerName != null && !playerName.isEmpty()) {
                        return playerName;
                    }
                }
            } catch (Exception ignored) {
                // fall through to the short uuid
            }
        }
        return shortId(playerUuid);
    }

    private static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    private static String shortId(String uuid) {
        return uuid.length() > 8 ? uuid.substring(0, 8) : uuid;
    }

    @Nullable
    private static UUID readUuid(Path file) {
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
