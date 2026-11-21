# Translation pipeline

## Current experimental translation menu

The main Rusify screen has a separate **«Переводы и настройки»** page. It can:

- independently enable or disable the built-in Russian translations exposed in the menu, including Carry On, OfflineLAN, Armor HUD, Default Options, Let Sleeping Dogs Lie, Example Mod, Fusion, Better Search, and Advanced Loot Info;
- export each built-in translation as its own resource pack;
- enable or disable automatic activation of all built-in packs at client startup;
- open the installed-mod scan and export screen.

The auto-activation setting is stored in `config/rusify-client.json`. Selected translations are combined into the `resourcepacks/Rusify-Translations` resource pack, which has a Rusify ownership marker. Existing directories without that marker are left untouched. The resource pack does not modify third-party mod JAR files.

## Built-in translation: Carry On

The included Russian file is based on the official Carry On project localization for Minecraft 1.21.1. The upstream `ru_ru.json` already translates all 12 keys present in the matching `en_us.json`; Rusify packages those reviewed strings as an offline resource pack.

- Mod ID: `carryon`
- Target namespace: `assets/carryon/lang/ru_ru.json`
- Target game: Minecraft 1.21.1
- Upstream project: https://github.com/Tschipp/CarryOn
- License: LGPL-3.0

## Built-in translation: Controlling

- Mod ID / language-key namespace: `controlling`
- Target namespace: `assets/controlling/lang/ru_ru.json`
- Included strings: 12 (all keys in the upstream English file)
- Target game: Minecraft 1.21.1, NeoForge
- Upstream project: https://github.com/jaredlll08/Controlling/tree/1.21.1
- Source revision: `7be58eb38962e9e1e1e932c84d9f57c65719a23c`
- License: MIT; the original license notice is preserved in [LICENSES/MIT-Controlling.txt](../LICENSES/MIT-Controlling.txt).

The upstream Russian file contained 10 of the 12 keys. Rusify keeps those translations and adds the two missing key-sorting labels. The English source key set is fully covered.
## Built-in translation: Configured

- Mod ID / language-key namespace: `configured`
- Target namespace: `assets/configured/lang/ru_ru.json`
- Included strings: 72 (all keys in the upstream English file)
- Target game: Minecraft 1.21.1, NeoForge
- Upstream project: https://github.com/MrCrayfish/Configured/tree/multiloader/1.21.1
- Source revision: `3f03e0d70663a8ddf22b00bfb283db4c912b56f0`
- License: LGPL-3.0-or-later; the original license text is preserved in [LICENSES/LGPL-3.0-Configured.txt](../LICENSES/LGPL-3.0-Configured.txt).

The upstream Russian file contained 62 entries, but most still had English text. Rusify provides a reviewed Russian translation for all 72 English keys. Format placeholders and the `§6` warning color code are preserved.

## Built-in translation: CreativeCore

- Mod ID / language-key namespace: `creativecore`
- Target namespace: `assets/creativecore/lang/ru_ru.json`
- Included strings: 47 (all keys in the upstream English file)
- Target game: Minecraft 1.21.1, NeoForge
- Upstream project: https://github.com/CreativeMD/CreativeCore/tree/1.21.x
- Source revision: `8d663de3916936fd469f54a9fffb2bf4eb29e09f`
- License: LGPL-3.0-or-later; the original license text is preserved in [LICENSES/LGPL-3.0-CreativeCore.txt](../LICENSES/LGPL-3.0-CreativeCore.txt).

The upstream Russian file contained 44 of the 47 English keys. Rusify preserves those translations and adds the missing boolean labels and sound-preview action. The English source key set is fully covered.

## Built-in translation: Sound Physics Remastered

- Mod ID / language-key namespace: `sound_physics_remastered`
- Target namespace: `assets/sound_physics_remastered/lang/ru_ru.json`
- Included strings: 72 (all keys in the upstream English file)
- Target game: Minecraft 1.21.1, NeoForge
- Upstream project: https://github.com/henkelmax/sound-physics-remastered/tree/1.21.1
- Source revision: `904527a720858a8a79f59b6a6bab2ebfafab0538`
- License: GPL-3.0; the original license text is already preserved in [LICENSES/GPL-3.0.txt](../LICENSES/GPL-3.0.txt).

The upstream Russian file contained 66 of the 72 English keys. Rusify adds the six missing settings and descriptions. The English source key set is fully covered.

## Built-in translation: AppleSkin

- Mod ID / language-key namespace: `appleskin`
- Target namespace: `assets/appleskin/lang/ru_ru.json`
- Included strings: 22 (all keys in the upstream English file)
- Target game: Minecraft 1.21.1, NeoForge
- Upstream project: https://github.com/squeek502/AppleSkin/tree/1.21-neoforge
- Source revision: `cd80d147c7a326ce67731f9ef546a65d72c98629`
- License: Unlicense (public domain dedication); see [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md).

The upstream Russian file contained 12 of the 22 keys. Rusify retains the existing Russian labels and completes the 10 missing tooltip descriptions. The English source key set is fully covered. The upstream release notes state that the 1.21 build also works on Minecraft 1.21.1.
## Built-in translation: OfflineLAN

- Mod ID: `offlinelan`
- Target namespace: `assets/offlinelan/lang/ru_ru.json`
- Included strings: 2
- Translation is packaged as an independent offline resource pack.

## Built-in translation: Armor HUD

- Mod ID / language-key namespace: `armorhud`
- Target namespace: `assets/armorhud/lang/ru_ru.json`
- Included strings: 33
- Covers the armor HUD widget, layout options, warning thresholds, and keybinding.

## Built-in translation: Default Options

- Mod ID / language-key namespace: `defaultoptions`
- Target namespace: `assets/defaultoptions/lang/ru_ru.json`
- Included strings: 7, including the configuration title and all current English option labels and tooltips.
- Upstream project: https://github.com/TwelveIterations/DefaultOptions

## Built-in translation: Let Sleeping Dogs Lie

- Mod ID / language-key namespace: `dogslie`
- Target namespace: `assets/dogslie/lang/ru_ru.json`
- Included strings: 12
- Upstream project: https://github.com/iChun/LetSleepingDogsLie
- The accepted values `NOBODY`, `OWNER`, `PLAYERS`, and `ANY_LIVING_ENTITY` are preserved exactly.

## Built-in translation: Example Mod (template namespace)

- Mod ID / language-key namespace: `examplemod`
- Target namespace: `assets/examplemod/lang/ru_ru.json`
- Included strings: 10
- Generic Example Mod / NeoForge template strings for projects using the `examplemod` namespace.

## Built-in translation: Fusion

- Mod ID / language-key namespace: `fusion`
- Target namespace: `assets/fusion/lang/ru_ru.json`
- Included strings: 8
- Upstream project: https://github.com/SuperMartijn642/Fusion

## Built-in translation: Advanced Loot Info

- Mod ID / language-key namespace: `ali`
- Target namespace: `assets/ali/lang/ru_ru.json`
- Included strings: 609
- The translation covers descriptions, enum labels, loot conditions and functions, item predicates, loot-table names, and EMI category names.
- Format placeholders such as `%s`, `%1$s`, and `%2$s` are preserved so dynamic values continue to render correctly.

## Built-in translation: Better Search

- Mod ID / language-key namespace: `bettersearch`
- Target namespace: `assets/bettersearch/lang/ru_ru.json`
- Included strings: 153
- Target game: Minecraft 1.21.1
- Upstream project: https://github.com/rivalzin/bettersearch
- Upstream source: `versions/mc1_21_1/common/src/main/resources/assets/bettersearch/lang/en_us.json`
- License: MIT
- Format placeholders such as `%s` and `Alt + %s` are preserved.

## Built-in translation: Borderless Window

- Mod ID / language-key namespace: `borderless`
- Target namespace: `assets/borderless/lang/ru_ru.json`
- Included strings: 21
- The existing translation is now available as an individual toggle in the built-in translation menu.

## Built-in translation: TaCZ (Timeless & Classics Zero)

- Mod ID / language-key namespace: `tacz`
- Target namespace: `assets/tacz/lang/ru_ru.json`
- Included strings: 489
- Target game: Minecraft 1.21.1
- Upstream project: https://github.com/MCModderAnchor/TACZ/tree/1.21.1
- Source revision: `0c18675405838eae703d71a87d553a0f111220a4`
- License: GPL-3.0; see [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md).

The translation follows the upstream 1.21.1 English key set. All 489 keys are present, including the 35 strings missing from the upstream Russian file. Format placeholders are checked against the English source and preserved.

## Built-in translation: Bookshelf

- Mod ID / language-key namespace: `bookshelf`
- Target namespace: `assets/bookshelf/lang/ru_ru.json`
- Included strings: 208 (all keys in the upstream English file)
- Target game: Minecraft 1.21.1, NeoForge
- Upstream project: https://github.com/Darkhax-Minecraft/Bookshelf/tree/1.21.1
- Source revision: `5dead24858668c3de411cec7956158dfc2aeeecf`
- License: LGPL-2.1-only; the original license text is preserved in [LICENSES/LGPL-2.1.txt](../LICENSES/LGPL-2.1.txt).

All 208 English source keys are covered. Formatting placeholders such as `%s` are preserved. The built-in entry is registered in Rusify's translation menu and export pipeline.

## Installed mod exports

Bulk export writes each mod into its own subfolder under `<game-directory>/Rusify/exports`:

- `Неполные переводы Rusify/<mod-id>/` — installed mods that have a built-in Rusify translation but still have untranslated source keys. The exported `ru_ru.json` starts with the bundled Rusify strings; missing source keys remain empty for further translation.
- `Частичный перевод/<mod-id>/` — other mods with at least one non-empty Russian translation, but with untranslated source keys remaining.
- `Нет перевода/<mod-id>/` — mods with no non-empty Russian translations.

Mods with complete Russian translations are not included in the incomplete-translation export. Mods without usable `en_us.json` strings are skipped.

## Planned translation engine

1. Curated Rusify translation packs (offline-first).
2. Persistent local translation cache.
3. Optional online fallback, disabled by default and requiring explicit user opt-in and provider selection.
4. Original source text if no translation is available.

No hidden network translation requests are made. Only localization strings may ever be sent to an explicitly selected provider; mod JARs and game folders must never be uploaded. Runtime translation should use resource packs/resource overlays and must not modify third-party JAR files.
