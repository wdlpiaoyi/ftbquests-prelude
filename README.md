# FTB Quests: Prelude

An unofficial addon mod for **FTB Quests** on **Minecraft 1.20.1 (Forge)**.

| | |
|---|---|
| Display name | FTB Quests: Prelude |
| Mod ID | `ftbquests_prelude` |
| Minecraft | 1.20.1 |
| Loader | Forge 47.x |
| Dependency | FTB Quests `2001.4.22` (`ftbquests`) |
| Status | Early development / scaffold |

## Requirements

- **JDK 17** — required by Minecraft 1.20.1 / ForgeGradle. If your default `java`
  is not 17, use the local launcher described below. It only affects this project
  and is not tracked by git.
- Network access on the first build (Gradle distribution, ForgeGradle, dependencies).

## Building

If your default JDK is 17:

```bat
gradlew.bat build
```

Otherwise, copy the template and point it at your JDK 17 installation:

```bat
copy gradlew-jdk17.bat.example gradlew-jdk17.bat
:: edit gradlew-jdk17.bat and set JAVA_HOME, then:
gradlew-jdk17.bat build
```

`gradlew-jdk17.bat` is git-ignored, so machine-specific paths never end up in the
repository. The built jar is placed in `build/libs/`.

Useful tasks (use `gradlew-jdk17.bat` instead of `gradlew.bat` if you need JDK 17):

```bat
gradlew.bat runClient      :: launch a dev client
gradlew.bat runServer      :: launch a dev server
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
├── src/main/resources/
└── Reference/     Third-party resources (reference source, jars, docs). Git-ignored.
```

## Dependencies

Compiled and run against the FTB maven releases:

| Artifact | Version |
|---|---|
| `dev.ftb.mods:ftb-quests-forge` | `2001.4.22` |
| `dev.ftb.mods:ftb-library-forge` | `2001.2.9` |
| `dev.ftb.mods:ftb-teams-forge` | `2001.3.0` |
| `dev.architectury:architectury-forge` | `9.1.12` |

> This project is not affiliated with or endorsed by FTB (Feed The Beast).
