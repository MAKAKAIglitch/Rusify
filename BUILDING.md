# Building and updating Rusify

## Windows one-click build

Run `build.bat` from the repository root.

The Windows builder is adapted from the working builder in [SaveWatch](https://github.com/MAKAKAIglitch/SaveWatch):

- Searches for a full JDK 21 in `JAVA_HOME`, `PATH`, common Java installation folders, and common Prism Launcher runtime folders.
- Verifies both `java.exe` and `javac.exe`; only JDK 21 is accepted.
- If no suitable JDK exists, downloads Eclipse Temurin 21 using the official Adoptium API.
- Verifies the downloaded archive against Adoptium's SHA-256 checksum before extraction.
- Stores the isolated JDK in `.tools/jdk-21-temurin`; it does not modify system Java settings.
- Uses the pinned Gradle Wrapper 9.2.1 and the standard Gradle user cache.
- Reuses the local JDK, Gradle distribution, dependency cache, and build cache on later runs.
- Enables parallel Gradle work and the Gradle daemon for faster repeated builds.

First build may need internet access for JDK, Gradle, and NeoForge dependencies. Later builds reuse cached files.

Examples:

```bat
build.bat
build.bat clean build
build.bat runClient
```

The mod JAR is written to `build/libs/`.

## Update sources from GitHub

Run `update-sources.bat` from the Rusify project root. It works both in a Git clone and in a folder extracted from the GitHub ZIP — **Git for Windows is not required**.

The updater downloads the latest ZIP archive of the official `main` branch directly from GitHub over HTTPS, checks that the archive contains the expected project files, and copies the updated source files into the current folder. It keeps user/game data such as `mods`, `config`, `saves`, `logs`, `build`, `.tools`, and `.gradle` untouched.

Before replacing an existing source file that differs from GitHub, the updater copies the old file into `.rusify-backups/<timestamp>/`. It also removes obsolete project files only when they were listed in the manifest from a previous successful run. The updater does not invoke Git, reset the folder, or delete arbitrary files.

Requirements: Windows PowerShell 5.1 and an internet connection to GitHub. After updating, run `build.bat` if you want to compile the new source code.

## Caches

- JDK: project-local `.tools/jdk-21-temurin`.
- Gradle distribution and dependencies: standard user Gradle cache.
- Compiled Gradle task outputs: Gradle build cache.

These are reused between builds and should not be downloaded again unless missing or invalid.
