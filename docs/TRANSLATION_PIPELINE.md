# Future translation engine

This document records the intended translation pipeline so the feature is not lost.

## Translation priority

1. **Curated Rusify translation packs (offline-first).** Check our own reviewed translations first. Each pack must identify the mod ID, namespace, mod version range, source language, target language, and source-key fingerprints.
2. **Local translation cache.** Reuse translations previously reviewed or obtained from the online fallback. Never needlessly send the same string again.
3. **Online fallback.** If no curated or cached translation exists, offer an optional online provider. The user must enable it and choose the provider. Show what text will be sent before the first request.
4. **Original text.** If translation is unavailable, keep the original string; never replace it with an empty value.

## Translation pack requirements

- Open JSON format suitable for GitHub and community contributions.
- Versioned by mod version and translation schema.
- Keep source text alongside translated text to detect changed upstream strings.
- Track states: reviewed, draft, outdated, conflict.
- Preserve user edits; never overwrite them silently.
- Update packs from the official Rusify repository.
- Work completely offline when a local pack is available.

## Online fallback and privacy

- No hidden background translation requests.
- Online translation is opt-in and configurable.
- Send only the localization strings required for translation, never the mod JAR or the game folder.
- Add timeouts, retry limits, rate limits, and persistent caching.
- Allow users to exclude mods or namespaces.
- If offline or the provider fails, use the local pack/cache and then original text.

## Applying translations in Minecraft

Prefer a Rusify resource layer/resource pack that supplies `ru_ru.json` entries for the relevant namespaces. Do not modify third-party mod JAR files. Respect resource-pack priority and detect key conflicts.

## Future milestones

- [ ] Repository of reviewed Russian translation packs.
- [ ] Translation-pack downloader and updater.
- [ ] Mod-version and source-key matching.
- [ ] Persistent local translation cache.
- [ ] Configurable online provider with explicit user consent.
- [ ] Runtime resource layer for applying translations.
- [ ] UI states for reviewed / partial / online fallback / untranslated.

**Current scope:** Rusify only scans installed mods and exports language files. Automatic translation and runtime application are future milestones.
