# FTB Quests: Prelude

**Open the quest book without entering a world.**

An unofficial client-side addon for FTB Quests: browse and edit your local quest files from the main
menu, the world selection / creation screens and the world loading screen — and view a single-player
save's quest progress from the same book.

> This project is not affiliated with or endorsed by FTB (Feed The Beast).

## The problem it solves

FTB Quests' quest book can only be opened after you are in a world. Modpack authors tweaking quests or
checking progress, and anyone who wants a look at the quest line before starting a save, has to load
into a world first.

This mod moves that quest book outside the world.

## Features

- **Quest book outside a world** — a book button in the top-right corner of the title, world
  selection, world creation and world loading screens, plus a key binding (unbound by default; set it
  under Options → Controls → Key Binds).
- **Local editor mode** — when enabled, all quests are visible and editable: quests, chapters, tasks,
  rewards and reward tables. Changes are written directly to `<config>/ftbquests/quests`.
- **Safe writing** — debounced auto-save, save-on-close, and an automatic timestamped backup before
  every save (retention is configurable). Orphaned chapter / reward-table files are pruned.
- **Single-player save progress** — open any save's quest progress inside the book and switch between
  its teams. Each team row shows its member count; right-click a team for the member list.
- **Native look** — screens, themes and icons all reuse FTB Quests / FTB Library assets, so it matches
  the rest of FTB Quests. Resource packs that restyle FTB icons apply here too.

## Requirements

- Minecraft **1.20.1**
- Forge **47.x**
- **FTB Quests** (and its dependencies: FTB Library, FTB Teams, Architectury API)
- **Client-side** (`displayTest = IGNORE_ALL_VERSION`) — a client with this mod can join servers that
  do not have it

## Installation

1. Install Minecraft 1.20.1 + Forge 47.x.
2. Install FTB Quests and its dependencies.
3. Put `ftbquests_prelude-1.0.0.jar` into your `mods/` folder.

## Usage

**Opening the local quest book** — Click the book button in the top-right corner, or bind a key under
Options → Controls → Key Binds → FTB Quests: Prelude. When a world is highlighted on the select-world
screen (or a world is loading), the button opens **that save's progress** instead.

**Local quest progress** — Use the save icon button in the bottom-right button panel of the quest book.
If you are already viewing a save it opens that save's team list directly; otherwise it opens the save
picker first. Hover a team to see the member count, or **right-click** it to open the member list.

**Editor mode** — Toggle it with the editor button in the book, or set `editorModeDefault = true`.

## Configuration

`<config>/ftbq_prelude/ftbquests_prelude-common.toml`

- `backupCount` (default `10`, range `0`–`1000`) — number of timestamped backups to keep; `0` disables backups.
- `autoSaveOnClose` (default `true`) — save pending edits when the local quest screen closes.
- `autoSaveDebounceSeconds` (default `5`, range `1`–`300`) — seconds of inactivity before pending edits are written to disk.
- `editorModeDefault` (default `false`) — open the local quest book in editor mode.
- `showEntryButtons` (default `true`) — show the book button on the menu screens (the key binding still works).
- `showSaveProgressButton` (default `true`) — show the progress button inside the quest book.

## Data and backups

- `<config>/ftbquests/quests` — the local quest data that is read and edited.
- `<config>/ftbq_prelude/backups` — timestamped backups of the quests folder.
- `<save>/ftbquests/<team-uuid>.snbt` — a single-player save's quest progress (read only).
- `<save>/ftbteams/...` — FTB Teams data (read only, used to list teams and members).

## Known limitations

- **Not a replacement for server sync**: in multiplayer, quest data and progress remain
  server-authoritative.
- Quest files that were never cached locally may be unavailable, so multiplayer progress is not
  guaranteed.
- Editing multiplayer quests from the menu is not supported — use the in-world quest book.
- Still a prototype: not every flow reuses the full native UI.
- Team names and members are resolved from local save data; players never seen on this machine may
  appear as a short UUID.

## License and credits

- License: **MIT**.
- Built on, and thanks to: FTB Quests, FTB Library, FTB Teams, Architectury API.
- Source and issue tracker: https://github.com/wdlpiaoyi/ftbquests-prelude
