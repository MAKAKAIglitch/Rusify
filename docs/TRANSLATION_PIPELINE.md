# Translation pipeline

## Current experimental translation menu

The main Rusify screen has a separate **«Переводы и настройки»** page. It can:

- independently enable or disable the built-in Carry On, OfflineLAN, and Advanced Loot Info Russian resource packs;
- export each built-in translation as its own resource pack;
- enable or disable automatic activation of all built-in packs at client startup;
- open the installed-mod scan and export screen.

The auto-activation setting is stored in `config/rusify-client.json`. Each pack is generated under its own `resourcepacks/Rusify-<mod-id>-Translation` directory and has a Rusify ownership marker. Existing directories without that marker are left untouched. The resource packs do not modify third-party mod JAR files.

## Built-in translation: Carry On

The included Russian file is based on the official Carry On project localization for Minecraft 1.21.1. The upstream `ru_ru.json` already translates all 12 keys present in the matching `en_us.json`; Rusify packages those reviewed strings as an offline resource pack.

- Mod ID: `carryon`
- Target namespace: `assets/carryon/lang/ru_ru.json`
- Target game: Minecraft 1.21.1
- Upstream project: https://github.com/Tschipp/CarryOn
- License: LGPL-3.0

## Built-in translation: OfflineLAN

- Mod ID: `offlinelan`
- Target namespace: `assets/offlinelan/lang/ru_ru.json`
- Included strings: 2
- Translation is packaged as an independent offline resource pack.

## Built-in translation: Advanced Loot Info

- Mod ID / language-key namespace: `ali`
- Target namespace: `assets/ali/lang/ru_ru.json`
- Included strings: 609
- The translation covers descriptions, enum labels, loot conditions and functions, item predicates, loot-table names, and EMI category names.
- Format placeholders such as `%s`, `%1$s`, and `%2$s` are preserved so dynamic values continue to render correctly.

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
