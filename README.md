# FTB Quests: Prelude

An unofficial client-side addon for **FTB Quests** on **Minecraft 1.20.1 (Forge)**.
It lets you open the quest book **outside a world** — at the main menu, on the world
selection/creation screens and on the loading screen — and browse a single-player
save's quest progress from the same book.

| | |
|---|---|
| Display name | FTB Quests: Prelude |
| Mod ID | `ftbquests_prelude` |
| Minecraft | 1.20.1 |
| Loader | Forge 47.x |
| Side | Client (works in single-player; no effect on a server) |
| Depends on | FTB Quests (`ftbquests`) |
| Status | Working prototype |

> This project is not affiliated with or endorsed by FTB (Feed The Beast).

## Features

- **Local quest book outside a world.** A small book button in the top-right corner
  of the title, world selection, world creation and world loading screens opens the
  quest book using the quests in `<config>/ftbquests/quests`.
- **Local editing.** In editor mode, changes (create/edit/delete quests, tasks,
  rewards, chapters, reward tables) are applied directly to that folder, with
  debounced auto-save, save-on-close and automatic timestamped backups.
- **Single-player save progress.** Open any save's quest progress inside the book and
  switch between its teams. Selecting a world on the select-world screen (or the
  loading screen) opens that save's progress by default.
- **Native UI.** The book and the save/team pickers use FTB Quests / FTB Library
  screens and themes, so they look and behave like the rest of FTB Quests.

## Requirements

- **Minecraft 1.20.1** with **Forge 47.x**.
- **FTB Quests** for 1.20.1 (`2001.4.22` was used for development). FTB Library,
  FTB Teams and Architectury are pulled in by FTB Quests and must be present.
- **JDK 17** only if you want to build from source (see [Building](#building)).

## Installation

1. Install Minecraft 1.20.1 + Forge 47.x.
2. Put **FTB Quests** (and its dependencies) in your `mods/` folder.
3. Put `ftbquests_prelude-<version>.jar` in the same `mods/` folder.
4. Launch the client. The mod is client-side; it does not need to be on a server.

## Usage

### Opening the local quest book

- Click the **book button** in the top-right corner of the title, world selection,
  world creation or world loading screen.
- Or press the key binding (unbound by default — set it in
  **Options → Controls → Key Binds → FTB Quests: Prelude**).

When a world is highlighted on the select-world screen, or a world is loading, the
button opens that **save's progress** instead of the plain local book.

### Local quest progress

Inside the quest book, the **floppy-disk button** in the bottom-right button panel
opens the progress picker:

- If you are already viewing a save, it opens the **team picker** for that save.
  Each team shows its name; hover to see the member count and names, or
  **right-click** a team to open its member list. Pick a team to load its progress.
- Otherwise it opens the **save picker** first. Use **Switch save...** in the team
  picker to move to another save.

Switching teams updates the open book in place, so <kbd>Esc</kbd> returns to the
screen you came from in one press.

### Editor mode

Editor mode makes all quests visible/editable. Toggle it with the editor button in
the quest book, or set `editorModeDefault = true` in the config to start in it.

## Configuration

Located at `<config>/ftbq_prelude/ftbquests_prelude-common.toml`.
It is a client-side mod, so these values only affect your client.

| Option | Default | Range | Description |
|---|---|---|---|
| `backupCount` | `10` | `0`–`1000` | Timestamped backups of the quests folder to keep. `0` disables backups. |
| `autoSaveOnClose` | `true` | — | Write pending local edits when the local quest screen closes. |
| `autoSaveDebounceSeconds` | `5` | `1`–`300` | Seconds of inactivity before pending edits are written to disk. |
| `editorModeDefault` | `false` | — | Open the local quest book directly in editor mode. |
| `showEntryButtons` | `true` | — | Show the top-right book button on the menu screens (the key binding still works). |
| `showSaveProgressButton` | `true` | — | Show the floppy-disk progress button inside the quest book. |

## Data and backups

| Path | Contents |
|---|---|
| `<config>/ftbquests/quests` | Local quest data that is read and edited. |
| `<config>/ftbq_prelude/backups` | Timestamped backups of the quests folder (this mod's own directory). |
| `<save>/ftbquests/<team-uuid>.snbt` | A single-player save's quest progress, read only. |
| `<save>/ftbteams/...` | FTB Teams data, read only, used to list teams and members. |

Backups are taken before each save and pruned to `backupCount`; nothing else writes
to the config folder.

## Building

Requires **JDK 17** (Minecraft 1.20.1 / ForgeGradle). If your default `java` is 17:

```bat
gradlew.bat build
```

Otherwise copy the launcher template and point it at your JDK 17 installation:

```bat
copy gradlew-jdk17.bat.example gradlew-jdk17.bat
:: edit gradlew-jdk17.bat and set JAVA_HOME, then:
gradlew-jdk17.bat build
```

`gradlew-jdk17.bat` is git-ignored, so machine-specific paths never end up in the
repository. The built jar is placed in `build/libs/`.

Useful tasks (use `gradlew-jdk17.bat` instead of `gradlew.bat` if you need JDK 17):

```bat
gradlew.bat runClient          :: launch a dev client
gradlew.bat runServer          :: launch a dev server
gradlew.bat genIntellijRuns
gradlew.bat genEclipseRuns
```

## Repository layout

```
.
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── src/main/java/dev/wdlpiaoyi/ftbquestsprelude/
│   ├── backup/     Backup/restore of the quests folder
│   ├── client/     Screens, buttons, entry points
│   ├── compat/     All FTB Quests access, isolated behind this layer
│   ├── config/     Forge config spec
│   └── mixin/      Client mixins into FTB Quests
├── src/main/resources/
└── Reference/      Third-party resources (reference source, jars, docs). Git-ignored.
```

All FTB Quests API calls are confined to `compat/`; the rest of the mod talks to it
through that layer.

## Dependencies

Compiled and run against the FTB maven releases:

| Artifact | Version |
|---|---|
| `dev.ftb.mods:ftb-quests-forge` | `2001.4.22` |
| `dev.ftb.mods:ftb-library-forge` | `2001.2.9` |
| `dev.ftb.mods:ftb-teams-forge` | `2001.3.0` |
| `dev.architectury:architectury-forge` | `9.1.12` |

## Known limitations

- This is **not** a replacement for FTB Quests' server sync. In multiplayer, quest
  data and progress remain server-authoritative.
- Quest files that have not been cached locally may not be available offline;
  multiplayer progress is not guaranteed.
- Editing multiplayer quests from the menu is not supported — use the in-world book.
- The prototype does not reuse the entire native UI for every flow, so some
  interactions differ from the in-world book.
- Team names and members are resolved from local save data; players who were never
  seen locally may appear as a short UUID.
