# FTB Quests: Prelude

[English](README.md) | **简体中文**

**FTB Quests** 的非官方客户端附属模组，适用于 **Minecraft 1.20.1 (Forge)**。
它让你**在世界之外**打开任务书 —— 主菜单、选择世界 / 创建世界界面、世界加载界面 ——
并在同一本书里查看单人存档的任务进度。

| | |
|---|---|
| 显示名称 | FTB Quests: Prelude |
| Mod ID | `ftbquests_prelude` |
| Minecraft | 1.20.1 |
| 加载器 | Forge 47.x |
| 端 | 客户端（单人可用；服务端不加也无影响） |
| 依赖 | FTB Quests (`ftbquests`) |
| 状态 | 可用原型 |

> 本项目与 FTB（Feed The Beast）官方无关，未获其认可或背书。

## 功能

- **世界之外打开本地任务书。** 标题界面、选择世界、创建世界、世界加载界面的右上角有一个
  小书本按钮，用它打开基于 `<config>/ftbquests/quests` 的任务书。
- **本地编辑。** 编辑模式下，创建 / 编辑 / 删除任务、任务目标、奖励、章节、奖励表都会直接
  写入该文件夹，并带有防抖自动保存、关闭即保存和自动时间戳备份。
- **单人存档进度。** 在任务书内打开任意存档的任务进度，并在其队伍之间切换。在选择世界界面
  选中某个世界（或在加载界面）时，默认打开该存档的进度。
- **原生界面。** 任务书与存档 / 队伍选择界面使用 FTB Quests / FTB Library 的界面与主题，
  外观与交互与 FTB Quests 其余部分一致。

## 环境要求

- **Minecraft 1.20.1** + **Forge 47.x**。
- **FTB Quests**（1.20.1；开发时使用 `2001.4.22`）。FTB Library、FTB Teams 与 Architectury
  由 FTB Quests 一并引入，必须存在。
- **JDK 17**：仅从源码构建时需要（见 [构建](#构建)）。

## 安装

1. 安装 Minecraft 1.20.1 + Forge 47.x。
2. 把 **FTB Quests**（及其依赖）放进 `mods/` 文件夹。
3. 把 `ftbquests_prelude-<version>.jar` 放进同一个 `mods/` 文件夹。
4. 启动客户端。本模组是客户端模组，服务端无需安装。

## 使用

### 打开本地任务书

点击标题、选择世界、创建世界或世界加载界面右上角的**书本按钮**。

在选择世界界面选中某个世界、或世界正在加载时，按钮会打开**该存档的进度**，而不是普通本地任务书。

### 本地任务进度

在任务书内，右下角按钮面板中的**保存图标按钮**会打开进度选择界面：

- 如果你正在查看某个存档，它会直接打开该存档的**队伍选择**。每行显示队伍名；悬停可看成员
  数量与名字，或**右键**队伍打开成员列表。选择队伍即可加载其进度。
- 否则会先打开**存档选择**。在队伍选择里用 **切换存档…** 换到其它存档。

切换队伍会就地更新已打开的任务书，因此按 <kbd>Esc</kbd> 一次即可回到来源界面。

### 编辑模式

编辑模式让所有任务可见 / 可编辑。用任务书里的编辑按钮切换，或在配置中设置
`editorModeDefault = true` 以默认开启。

## 配置

位置：`<config>/ftbq_prelude/ftbquests_prelude-common.toml`。
本模组是客户端模组，因此这些值只影响你的客户端。

| 选项 | 默认 | 范围 | 说明 |
|---|---|---|---|
| `backupCount` | `10` | `0`–`1000` | 保留的任务文件夹时间戳备份数量。`0` 表示关闭备份。 |
| `autoSaveOnClose` | `true` | — | 关闭本地任务界面时写入待保存的编辑。 |
| `autoSaveDebounceSeconds` | `5` | `1`–`300` | 停止编辑多少秒后把待保存内容写入磁盘。 |
| `editorModeDefault` | `false` | — | 直接以编辑模式打开本地任务书。 |
| `showEntryButtons` | `true` | — | 在菜单界面显示右上角书本按钮（唯一入口）。 |
| `showSaveProgressButton` | `true` | — | 在任务书内显示保存图标进度按钮。 |

## 数据与备份

| 路径 | 内容 |
|---|---|
| `<config>/ftbquests/quests` | 读取与编辑的本地任务数据。 |
| `<config>/ftbq_prelude/backups` | 任务文件夹的时间戳备份（本模组自己的目录）。 |
| `<save>/ftbquests/<team-uuid>.snbt` | 单人存档的任务进度，只读。 |
| `<save>/ftbteams/...` | FTB Teams 数据，只读，用于列出队伍与成员。 |

每次保存前会先备份，并按 `backupCount` 裁剪；除此之外不会向配置文件夹写入任何东西。

## 构建

需要 **JDK 17**（Minecraft 1.20.1 / ForgeGradle）。如果你的默认 `java` 是 17：

```bat
gradlew.bat build
```

否则复制启动器模板并指向你的 JDK 17 安装：

```bat
copy gradlew-jdk17.bat.example gradlew-jdk17.bat
:: 编辑 gradlew-jdk17.bat 并设置 JAVA_HOME，然后：
gradlew-jdk17.bat build
```

`gradlew-jdk17.bat` 已被 git 忽略，因此机器相关路径不会进入仓库。构建产物在 `build/libs/`。

常用任务（需要 JDK 17 时用 `gradlew-jdk17.bat` 代替 `gradlew.bat`）：

```bat
gradlew.bat runClient          :: 启动开发客户端
gradlew.bat runServer          :: 启动开发服务端
gradlew.bat genIntellijRuns
gradlew.bat genEclipseRuns
```

## 仓库结构

```
.
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── src/main/java/dev/wdlpiaoyi/ftbquestsprelude/
│   ├── backup/     任务文件夹的备份
│   ├── client/     界面、按钮、入口
│   ├── compat/     所有 FTB Quests 访问，隔离在此层之后
│   ├── config/     Forge 配置定义
│   └── mixin/      注入 FTB Quests 的客户端 mixin
├── src/main/resources/
└── Reference/      第三方资源（参考源码、jar、文档）。已被 git 忽略。
```

所有 FTB Quests API 调用都限制在 `compat/` 内；其余代码通过该层与其交互。

## 依赖

编译与运行基于 FTB maven 发布：

| 构件 | 版本 |
|---|---|
| `dev.ftb.mods:ftb-quests-forge` | `2001.4.22` |
| `dev.ftb.mods:ftb-library-forge` | `2001.2.9` |
| `dev.ftb.mods:ftb-teams-forge` | `2001.3.0` |
| `dev.architectury:architectury-forge` | `9.1.12` |

## 已知限制

- 这**不能**替代 FTB Quests 的服务端同步。多人游戏中，任务数据与进度仍以服务端为准。
- 未在本地缓存过的任务文件可能离线不可用；多人进度不保证。
- 不支持在菜单中编辑多人任务 —— 请使用世界内的任务书。
- 原型阶段未对每个流程都复用完整原生 UI，因此部分交互与世界内任务书不同。
- 队伍名与成员从本地存档数据解析；从未在本机出现过的玩家可能显示为短 UUID。

## 更新日志

见 [CHANGELOG.md](CHANGELOG.md)。

## 许可证

MIT —— 见 [LICENSE](LICENSE)。
