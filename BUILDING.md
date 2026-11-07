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

Run `update-sources.bat` from a Git clone of the official repository.

The updater checks the repository origin, current branch (`main`), local modifications, and local-only commits. It updates with `git merge --ff-only`; it does not reset, force-pull, or overwrite uncommitted work.

Requirements: Git for Windows and Windows PowerShell 5.1. A ZIP-extracted folder is not a Git clone; clone the repository before using the updater.

## Caches

- JDK: project-local `.tools/jdk-21-temurin`.
- Gradle distribution and dependencies: standard user Gradle cache.
- Compiled Gradle task outputs: Gradle build cache.

These are reused between builds and should not be downloaded again unless missing or invalid.
