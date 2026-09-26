A client-side mod for Minecraft 1.20.1 (Forge) that opens the **FTB Quests** quest book outside a
world: from the main menu, the world selection and creation screens, and the world loading screen.
It also displays a single-player save's quest progress in the same book.

**Requires [FTB Quests](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge)** and its
dependencies (FTB Library, FTB Teams, Architectury API). Client-side only — a server does not need it.

## What it adds

FTB Quests' book can only be opened from inside a world. This mod adds entry points for it in screens
where it is normally unreachable:

| Screen | What the button opens |
| --- | --- |
| Title screen | The local book — the quests in `<config>/ftbquests/quests` |
| World selection | The highlighted save's quest progress |
| World creation | The local book |
| World loading | The world being loaded, if it has saved progress |

## Local editing

Editor mode makes every quest visible and editable: quests, chapters, tasks, rewards and reward
tables. Changes are written straight to `<config>/ftbquests/quests`. Turn editor mode on with the
book's editor button, or with `editorModeDefault = true` in the config.

The quest files can also be edited outside the game: the **reload button** in the book's button panel
re-reads them from disk, keeping the current save/team and the screen you came from.

Writing is guarded, so a mistake is recoverable:

- debounced auto-save while you edit;
- an extra save when the screen closes;
- a timestamped backup before every save, with a configurable retention count;
- orphaned chapter and reward-table files are cleaned up on save.

## Single-player save progress

The save icon button in the book's bottom-right button panel opens the progress picker:

- while you are viewing a save, it opens that save's team list;
- otherwise it opens a save picker first, and the team list has a **Switch save…** entry.

Each team row shows its member count. Hover it for the member names, or right-click it for the full
member list. Selecting a team loads that team's progress into the open book.

## Configuration

The config file is `<config>/ftbq_prelude/ftbquests_prelude-common.toml`.

| Option | Default | Range | Description |
| --- | --- | --- | --- |
| `backupCount` | `10` | 0–1000 | Timestamped backups to keep. `0` disables backups. |
| `autoSaveOnClose` | `true` | — | Save pending edits when the screen closes. |
| `autoSaveDebounceSeconds` | `5` | 1–300 | Seconds of inactivity before pending edits are saved. |
| `editorModeDefault` | `false` | — | Open the local book in editor mode. |
| `showEntryButtons` | `true` | — | Show the menu-screen buttons (the only entry point). |
| `showSaveProgressButton` | `true` | — | Show the progress button inside the book. |

## Compatibility

- **Minecraft** 1.20.1
- **Loader** Forge 47.x
- **Side** Client only (`displayTest = IGNORE_ALL_VERSION`, so clients may join servers without it)
- **Dependencies** FTB Quests `2001.4.x` or newer (built against `2001.4.22`), plus FTB Library,
  FTB Teams and Architectury API

## Known limitations

- This mod does **not** replace FTB Quests' server sync. In multiplayer, quest data and progress
  remain server-authoritative.
- Quest files that were never cached locally may be unavailable, so multiplayer progress is not
  guaranteed.
- Editing multiplayer quests from these screens is not supported — use the in-world book.
- Not every flow reuses the full native UI, so some interactions differ from the in-world book.
- Team names and members are read from local save data; players never seen on this machine can show
  as a short UUID.
- Outside a world a few things cannot match the in-world book: modded biomes only appear once a world
  has been joined in the session; the fluid picker shows no fluid icons; and `T` / `S` / `D` (edit quest
  title / subtitle / description), the dimension task icon and the reward-table close "x" do not work.
  All of these behave the same way in unmodified FTB Quests on this machine, so they are left alone.

## Credits and license

- License: **MIT**.
- Built on **FTB Quests**, **FTB Library**, **FTB Teams** and the **Architectury API**; thanks to
  their authors.
- This project is not affiliated with or endorsed by FTB (Feed The Beast).
