# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.5] - 2026-09-26

### Changed

- **Removed the key binding.** It also worked from inside a world, where this mod has no business
  opening anything. The menu button is the intended (and now only) entry point.
- **`editorModeDefault` is off by default again**, so a fresh install spoils nothing for players. The
  option stays wired up and now actually works (see 1.0.4).

### Notes

- `T` / `S` / `D` (edit quest title / subtitle / description) do not work in the local book even with
  editor mode on. They do not work in unmodified FTB Quests either, so this is an upstream bug rather
  than something this mod introduces.

## [1.0.4] - 2026-09-26

### Fixed

- **`editorModeDefault` did nothing.** The option was declared but never read, so the local quest book
  always opened with editor mode off. That made FTB Quests hide its editing UI, exactly as it does for
  a player without edit rights: the key reference showed only the general controls, and the `T` / `S` /
  `D` quest text shortcuts were inert. The option is now wired up, and defaults to **on**.
- The local quest data log line now also reports whether editor mode is on.

### Notes

- An existing `ftbq_prelude/ftbquests_prelude-common.toml` keeps its previous `editorModeDefault`
  value, so set it to `true` (or delete the file) if you had it on `false`.

## [1.0.3] - 2026-09-26

### Fixed

- **Icons were still invisible in the progress picker (and the book's progress button).** FTB Library
  draws icons through {@code GuiHelper.drawTexturedRect}, which drives {@code RenderSystem} and
  {@code Tesselator} by hand; render-optimisation mods that take over the vertex pipeline (Accelerated
  Rendering, Florescent, Chloride, ImmediatelyFast, ...) can make that silently draw nothing.
  `PreludeIcons` now uses `BlitIcon`, which draws through the vanilla {@code GuiGraphics.blit} path -
  the same path this mod's menu button already used successfully in those packs.

## [1.0.2] - 2026-09-26

### Fixed

- **The in-world quest book is no longer modified.** Every FTB Quests UI change is now guarded by
  "the book on screen is the local one" (`LocalQuestSession.isLocalBook()`), and the local session is
  dropped when a world is joined. Previously the edit-mode button stayed modified in-world and the
  save-progress button also appeared there.
- **Claimed rewards were not recognised.** Outside a world `Minecraft.player` is null, so per-player
  lookups (claimed rewards, pinned quests) fell back to `Util.NIL_UUID` and never matched the save's
  data, which is keyed by the real player UUID. They now resolve to the local account's UUID.
- **"Collect rewards" and the auto-pin toggle** are hidden while the local book is open; both need a
  server to act on, and the first one otherwise just opened a dead screen.

## [1.0.1] - 2026-09-26

### Fixed

- **Missing save icon.** The progress buttons and picker rows looked up FTB Quests'
  `ThemeProperties.SAVE_ICON`, which resolves to an *empty* icon when the active theme does not
  define `save_icon` (`IconProperty` defaults to `Color4I.empty()`), so nothing was drawn. They now
  reference `ftbquests:textures/gui/save.png` and `ftbteams:textures/teams.png` directly.
- **Progress button did nothing for a single-team save.** It silently re-applied the team that was
  already displayed, which looks like the click was ignored. It now always opens the picker, so the
  *Switch save...* entry stays reachable.
- **"Paste quest" and "paste chapter image" were ignored outside a world.** `CopyQuestMessage` and
  `CopyChapterImageMessage` are now applied to the local quest file, mirroring the server logic.
- **The quest book's "save" button did nothing.** `ForceSaveMessage` now writes the local quest file
  to disk immediately and shows a toast.

### Notes

- Messages that need a server are still ignored: reward claiming, task submission, progress resets
  and per-player pinning.

## [1.0.0] - 2026-09-26

First release.

### Added

- **Local quest book outside a world.** An entry button in the top-right corner of the title,
  world selection, world creation and world loading screens, plus a key binding (unbound by
  default) to open the quest book from `<config>/ftbquests/quests`.
- **Local editor mode.** Create, edit and delete quests, tasks, rewards, chapters and reward
  tables; changes are applied directly to the local quest folder.
- **Safe local writing.** Debounced auto-save, save-on-close, and automatic timestamped backups
  under `<config>/ftbq_prelude/backups`, with orphan chapter/reward-table pruning.
- **Single-player save progress.** Reads `<save>/ftbquests/<team-uuid>.snbt` and shows it in the
  quest book. FTB-style save and team pickers; team rows show the member count and list the
  members on right-click.
- Opening from the select-world screen (highlighted save) or the loading screen defaults to that
  save's progress.
- **Config** at `<config>/ftbq_prelude/ftbquests_prelude-common.toml`: `backupCount`,
  `autoSaveOnClose`, `autoSaveDebounceSeconds`, `editorModeDefault`, `showEntryButtons`,
  `showSaveProgressButton`.
- **Localisation:** English (`en_us`) and Simplified Chinese (`zh_cn`).
- **Project logo**, generated by `tools/GenLogo.java`.

### Notes

- Client-side mod (`displayTest = IGNORE_ALL_VERSION`); it can be installed client-side and join
  servers that do not have it.
- It is not a replacement for FTB Quests' server sync; multiplayer data and progress remain
  server-authoritative.
- Icons reuse FTB Quests' and FTB Teams' own themed icons, so resource packs and FTB Quests themes
  that override them apply here as well.
