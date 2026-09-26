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
- Outside a world a few pickers cannot match the in-world book: modded biomes only appear after a
  world has been joined in the session, and the fluid picker shows no fluid icons. Both behave the
  same way in unmodified FTB Quests.

## Credits and license

- License: **MIT**.
- Built on **FTB Quests**, **FTB Library**, **FTB Teams** and the **Architectury API**; thanks to
  their authors.
- This project is not affiliated with or endorsed by FTB (Feed The Beast).

---

## 简体中文

Minecraft 1.20.1 (Forge) 的客户端模组：**不进入世界也能打开 FTB Quests 任务书** —— 主菜单、选择世界、创建世界、世界加载界面均可打开，并能在同一本书里查看单人存档的任务进度。

- **本地编辑**：编辑模式下所有任务可见可改（任务、章节、目标、奖励、奖励表），改动直接写入 `<config>/ftbquests/quests`；带防抖自动保存、关闭即保存、保存前时间戳备份，并清理孤立文件。
- **存档进度**：任务书右下角的保存图标按钮可切换存档与队伍；队伍行显示成员数，悬停看成员名，右键展开成员列表。
- **配置**：`<config>/ftbq_prelude/ftbquests_prelude-common.toml`（备份数量、自动保存、编辑模式默认、入口开关等）。
- **需要**：FTB Quests 及其依赖（FTB Library、FTB Teams、Architectury API）；仅需客户端安装。
- **注意**：不替代服务端同步，多人数据与进度仍以服务端为准；世界外有个别限制（模组群系需进过一次世界、流体选择器无图标），原版 FTBQ 表现一致。许可：MIT。

---

## Links

- Source code and issue tracker: <https://github.com/wdlpiaoyi/ftbquests-prelude>
- Changelog: <https://github.com/wdlpiaoyi/ftbquests-prelude/blob/main/CHANGELOG.md>
