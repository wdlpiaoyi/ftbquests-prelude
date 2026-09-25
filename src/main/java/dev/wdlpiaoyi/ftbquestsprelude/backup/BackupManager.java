package dev.wdlpiaoyi.ftbquestsprelude.backup;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Filesystem backups of the quest data, stored under
 * {@code <config>/ftbq_prelude/backups/<label>-<timestamp>/}.
 *
 * <p>Backups are plain directory copies so they stay inspectable and can be diffed / committed.
 */
public final class BackupManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private BackupManager() {
    }

    /** Root directory for all backups: {@code <config>/ftbq_prelude/backups}. */
    public static Path getRootDir() {
        return FMLPaths.CONFIGDIR.get().resolve("ftbq_prelude").resolve("backups");
    }

    /** Creates the backup root directory if it does not exist yet. */
    public static void ensureRootDir() {
        Path root = getRootDir();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            LOGGER.error("[Prelude] Could not create backup directory {}", root, e);
        }
    }

    /**
     * Copies an entire directory into a new timestamped folder under the backup root.
     *
     * @param sourceDir directory to back up (e.g. the {@code ftbquests/quests} folder)
     * @param label     short label used as the backup folder prefix
     * @return the created backup directory, or empty if the source does not exist / the copy failed
     */
    public static Optional<Path> backupDirectory(Path sourceDir, String label) {
        if (sourceDir == null || !Files.isDirectory(sourceDir)) {
            LOGGER.warn("[Prelude] Skipping backup: {} is not a directory", sourceDir);
            return Optional.empty();
        }

        ensureRootDir();
        String safeLabel = (label == null || label.isBlank()) ? "quests" : label.replaceAll("[^a-zA-Z0-9_-]", "_");
        Path target = getRootDir().resolve(safeLabel + "-" + LocalDateTime.now().format(TIMESTAMP));

        try {
            copyRecursively(sourceDir, target);
            LOGGER.info("[Prelude] Backed up {} -> {}", sourceDir, target);
            return Optional.of(target);
        } catch (IOException e) {
            LOGGER.error("[Prelude] Backup of {} failed", sourceDir, e);
            return Optional.empty();
        }
    }

    /** Keeps only the newest {@code keep} backups under the root and deletes older ones. */
    public static void prune(int keep) {
        if (keep < 0) {
            return;
        }

        Path root = getRootDir();
        if (!Files.isDirectory(root)) {
            return;
        }

        try (Stream<Path> stream = Files.list(root)) {
            List<Path> backups = stream
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .toList();

            for (int i = keep; i < backups.size(); i++) {
                deleteRecursively(backups.get(i));
            }
        } catch (IOException e) {
            LOGGER.error("[Prelude] Pruning backups in {} failed", root, e);
        }
    }

    private static void copyRecursively(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }

        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.deleteIfExists(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
