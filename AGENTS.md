# AGENTS.md

Client-side Forge **1.20.1** addon for FTB Quests. Java 17, ForgeGradle 6 + MixinGradle.
No unit tests, no CI.

## Build & run — the JDK 17 trap

The default `java` may not be 17 (on this machine it is 25), and the build fails with it. Use the
local launcher, which is git-ignored and sets `JAVA_HOME` to a JDK 17:

```bat
.\gradlew-jdk17.bat build
.\gradlew-jdk17.bat runClient
```

- If `gradlew-jdk17.bat` is missing, copy `gradlew-jdk17.bat.example` and set `JAVA_HOME` in it.
- **Never** add `org.gradle.java.home` to `gradle.properties` — it is a machine-specific absolute
  path and would break every other clone. That is exactly why the launcher is git-ignored.
- Only `runClient` / `runServer` exist (no `runData`, no gametests).

## Verifying changes

There are no tests (`src/test` is empty) and no CI. Manual in-game checks are the only real
verification:

1. `.\gradlew-jdk17.bat build`
2. `.\gradlew-jdk17.bat runClient`, then exercise the feature.

`runClient` logs to stdout; the useful signal is the `mixin/` apply lines and `[Prelude] ...`
messages. Dev runtime is under `run/` (git-ignored):

- quest data under test: `run/config/ftbquests/quests`
- backups: `run/config/ftbq_prelude/backups`
- generated config: `run/config/ftbq_prelude/ftbquests_prelude-common.toml`

## Layout — where code goes

- `compat/` — **the only package allowed to reference FTB Quests types.** Keep it that way; callers
  use only the boolean / `Path` API. `FTBQuestsCompat#canUseLocalQuestBook()` gates every entry
  point. Rules are spelled out in `compat/package-info.java`.
  - `LocalQuestSession` — central session: loads a local `ClientQuestFile`, applies a save's team,
    debounced save + backup + orphan pruning, plus `openBook` / `applySaveProgress` /
    `getCurrentSaveRoot`.
  - `LocalEditBridge` — maps intercepted FTBQ C2S edit messages to client-side appliers when not
    connected. `SaveProgress` — reads `<save>/ftbquests` + `<save>/ftbteams` data.
- `client/` — screens, buttons, entry events. `PreludeIcons` centralises icon sources.
- `mixin/` — injections into FTB Quests and vanilla.

## Mixin rules (learned the hard way)

- FTB Quests / mod targets: pass `remap = false` on the injector / accessor (`method = "..."`,
  `@Accessor`). The annotation processor errors out when it cannot map a mod method.
- Vanilla `@At` targets: keep `remap = true` (the default), e.g.
  `@At(value = "INVOKE", remap = true, target = "Lnet/minecraft/...")`.
- Every mixin must be listed in `src/main/resources/ftbquests_prelude.mixins.json`. All current
  mixins are in the `client` array (so a dedicated server is safe); do not move them into `mixins`
  unless they are genuinely server-side.

## FTB Quests integration gotchas

- `ClientQuestFile.INSTANCE` is public static, but its `invalid` field is protected — check
  `ClientQuestFile.exists()` instead.
- **Every FTBQ UI change must be guarded by `LocalQuestSession.isLocalBook()`, never `isActive()`**,
  so the in-world book stays vanilla. `isActive()` is still true after joining a world (the session is
  only dropped by the `ClientPlayerNetworkEvent.LoggingIn` handler); `isLocalBook()` compares the
  session's file against `ClientQuestFile.INSTANCE`, which the server sync replaces.
- **A `@Redirect(method = X)` only reaches calls inside X's own method body.** A third-party mod that
  `@Inject`s at `HEAD` and calls `cancel()` both hides our redirect (the original body never runs) and
  runs its own code, so a null-player crash there cannot be fixed by redirecting. `UnsafeMods` (compat)
  is the fail-safe: it either neutralises the offender's own read-only switch at runtime (never the
  config file on disk) or refuses to open the local book with a message.
- Outside a world `Minecraft.player` is null. Per-player FTBQ lookups (claimed rewards, pins) must use
  `LocalQuestSession.viewPlayerUuid()` (the local account UUID) rather than `Util.NIL_UUID`, or the
  save's real data is never found.
- Open a **fresh `QuestScreen` per open** (that is what makes `prevScreen` / <kbd>Esc</kbd> behave).
  Switching a team must update the open screen in place + `refreshWidgets()` — never stack another
  screen, or <kbd>Esc</kbd> needs several presses.
- `LevelLoadingScreen.render` does not call `super.render`, so its widgets are clickable but never
  drawn; `PreludeClientEvents` draws our button manually in `ScreenEvent.Render.Post`.
- Prefer FTB-themed icons (`PreludeIcons`) over custom art, so resource packs and FTB Quests themes
  apply. They are drawn via `BlitIcon` (vanilla `GuiGraphics.blit`) **on purpose**: FTB Library's own
  `GuiHelper.drawTexturedRect` did not render them in the test pack. That only works for icons with a
  white colour (`blit` cannot tint), so `ImageIconMixin` also restricts itself to FTB Library's
  `AbstractThreePanelScreen` screens and skips tiled/UV-sliced/tinted icons. FTB Quests' own quest
  screen must stay excluded: redirecting it loses the tint and turns the shape textures white.

## Repo-specific files

- `Reference/FTB-Quests/` — read-only upstream FTB Quests source (git-ignored). The best place to
  check real FTBQ signatures and behaviour; never modify or commit it.
- `环境提示.md` — user-maintained machine / environment notes (git-ignored). Do not edit.
- `tools/GenLogo.java` — draws the logo (run with a JDK 17 `java`, then rebuild):
  `java tools/GenLogo.java <out.png> [size] [dipTop] [dipBot]`.
  `src/main/resources/logo.png` is 128 (mod list); `docs/curseforge-avatar.png` is 400, because
  CurseForge requires avatars to be exactly 400x400.
- `docs/curseforge-*.md|txt|png` — CurseForge listing assets: the description (English only - CurseForge
  is an English platform and its multi-language support is optional; the project's Source/Issues link
  fields cover the GitHub links, so the description carries no link section), the one-line summary and
  the 400x400 avatar.

## Metadata & publishing

- Mod metadata lives in `gradle.properties` (`mod_version`, `mod_license`, dependency versions);
  `src/main/resources/META-INF/mods.toml` uses `${...}` placeholders expanded by `processResources`.
  Change versions in `gradle.properties`, not in `mods.toml`.
- Client-only: mixins sit in the `client` array and `mods.toml` sets
  `displayTest = IGNORE_ALL_VERSION`, so clients may join servers without the mod.
- FTB artifacts come from `https://maven.ftb.dev/releases`; the required `ftbquests` dependency's
  version range is in `gradle.properties`.
- Released as v1.0.0; `main` tracks `origin` at https://github.com/wdlpiaoyi/ftbquests-prelude
  (`gh` is available and authenticated).
