# Translation pipeline

## Current experimental translation menu

The main Rusify screen now has a separate **«Переводы и настройки»** page. It can:

- enable or disable the built-in Carry On Russian resource pack;
- export the Carry On resource pack into the game's `resourcepacks/Rusify-Built-in-Translations` directory;
- enable or disable automatic activation of built-in packs on the next game launch;
- open the existing installed-mod scan and export screen.

The auto-activation setting is stored in `config/rusify-client.json`. The Carry On pack is a normal Minecraft resource pack and does not modify the mod JAR. Rusify only manages its own generated resource-pack directory; if a folder with the same name already exists and is not marked as Rusify-owned, it is left untouched.

## Built-in translation: Carry On

The included Russian file is based on the official Carry On project localization for Minecraft 1.21.1. The upstream `ru_ru.json` already translates all 12 keys present in the matching `en_us.json`; Rusify packages those reviewed strings as an offline resource pack.

- Mod ID: `carryon`
- Target namespace: `assets/carryon/lang/ru_ru.json`
- Target game: Minecraft 1.21.1
- Upstream project: https://github.com/Tschipp/CarryOn
- License: LGPL-3.0

## Installed mod exports

Bulk export writes each mod into its own subfolder under `<game-directory>/Rusify/exports`:

- `Частичный перевод/<mod-id>/` — mods with at least one non-empty Russian translation, but with untranslated source keys remaining.
- `Нет перевода/<mod-id>/` — mods with no non-empty Russian translations.

Mods with complete Russian translations are not included in the incomplete-translation export. Mods without usable `en_us.json` strings are skipped.

## Planned translation engine

1. Curated Rusify translation packs (offline-first).
2. Persistent local translation cache.
3. Optional online fallback, disabled by default and requiring explicit user opt-in and provider selection.
4. Original source text if no translation is available.

No hidden network translation requests are made. Only localization strings may ever be sent to an explicitly selected provider; mod JARs and game folders must never be uploaded. Runtime translation should use resource packs/resource overlays and must not modify third-party JAR files.
