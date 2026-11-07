# Rusify

**Rusify** — open-source инструмент для русской локализации Minecraft-модов.

## Первая экспериментальная версия

Клиентский мод для **Minecraft 1.21.1 + NeoForge 1.21.1**.

- Открывает главное меню клавишей `O`, командой `/rusify` или через настройки мода.
- Отдельная вкладка **«Переводы и настройки»** управляет готовыми переводами.
- В меню можно независимо включать/выключать встроенные переводы Carry On, OfflineLAN, Advanced Loot Info, Stone Zone, Gems Realm, Punchy!, Sable, Tectonic, Expanded Delight, Screenshot Viewer, Ragdoll Reactions, L_Ender's Delight, iChunUtil, Resourceful Config, Better Zoom, Create: More Automation, Create: Trimmed, Jump Over Fences, BreakingPlace 211 neo, Trimmed, Curios for Ammo Box, Better Advancements, RebindNarrator, Better Biome ReBlend, Sable: Ragdoll Corpse, When Dungeons Arise, [Let's Do] Vinery и Snow! Real Magic!.
- Все включённые переводы динамически объединяются в один ресурс-пак `Rusify-Translations`. При отключении мода его `ru_ru.json` удаляется из этого пакета; отдельные пакеты для каждого мода не создаются.
- При запуске Rusify автоматически переносит принадлежащие ему старые отдельные пакеты в общую структуру и удаляет их, не затрагивая чужие ресурс-паки.
- Встроенные переводы работают полностью офлайн и не требуют сторонних сервисов.
- Отдельный экран сканирует JAR-файлы из папки `mods` и экспортирует неполные/отсутствующие переводы.
- Исходные JAR-файлы модов не изменяются.

## Встроенные переводы

Все переводы находятся в одном динамическом ресурс-паке `resourcepacks/Rusify-Translations`. Для каждого мода используется собственный файл `assets/<namespace>/lang/ru_ru.json`; переключатель в меню добавляет или удаляет только соответствующий файл. Если отключены все переводы, общий пакет деактивируется и удаляется.

| Мод | Ключей | Файл локализации |
|---|---:|---|
| Carry On | 12 | `assets/carryon/lang/ru_ru.json` |
| OfflineLAN | 2 | `assets/offlinelan/lang/ru_ru.json` |
| Advanced Loot Info | 609 | `assets/ali/lang/ru_ru.json` |
| Stone Zone | 411 | `assets/stonezone/lang/ru_ru.json` |
| Gems Realm | 304 | `assets/gemsrealm/lang/ru_ru.json` |
| Punchy! | 275 | `assets/punchy/lang/ru_ru.json` |
| Sable | 128 | `assets/sable/lang/ru_ru.json` |
| Tectonic | 109 | `assets/tectonic/lang/ru_ru.json` |
| Expanded Delight | 119 | `assets/expandeddelight/lang/ru_ru.json` |
| Screenshot Viewer | 74 | `assets/screenshot_viewer/lang/ru_ru.json` |
| Ragdoll Reactions | 75 | `assets/ragdoll_reactions/lang/ru_ru.json` |
| L_Ender's Delight | 86 | `assets/lendersdelight/lang/ru_ru.json` |
| Held Item Info | 56 | `assets/held_item_info/lang/ru_ru.json` |
| Kiwi Library | 75 | `assets/kiwi/lang/ru_ru.json` |
| Ping Wheel | 61 | `assets/pingwheel/lang/ru_ru.json` |
| Sable Ragdolls | 43 | `assets/sable_player_ragdoll/lang/ru_ru.json` |
| AdvancedCoreInfo | 67 | `assets/aci/lang/ru_ru.json` |
| iChunUtil | 38 | `assets/ichunutil/lang/ru_ru.json` |
| Resourceful Config | 28 | `assets/resourcefulconfig/lang/ru_ru.json` |
| Better Zoom | 30 | `assets/betterzoom/lang/ru_ru.json` |
| Create: More Automation | 3 | `assets/create_more_automation/lang/ru_ru.json` |
| Create: Trimmed | 4 | `assets/create_trimmed/lang/ru_ru.json` |
| Jump Over Fences | 3 | `assets/jumpoverfences/lang/ru_ru.json` |
| BreakingPlace 211 neo | 3 | `assets/breakingplace211neo/lang/ru_ru.json` |
| Trimmed | 2 | `assets/trimmed/lang/ru_ru.json` |
| Curios for Ammo Box | 1 | `assets/curios_for_ammo_box/lang/ru_ru.json` |
| Better Advancements | 1 | `assets/betteradvancements/lang/ru_ru.json` |
| RebindNarrator | 1 | `assets/rebindnarrator/lang/ru_ru.json` |
| Better Biome ReBlend | 9 | `assets/betterbiomereblend/lang/ru_ru.json` |
| Sable: Ragdoll Corpse | 6 | `assets/ragdoll_corpse/lang/ru_ru.json` |
| When Dungeons Arise | 105 | `assets/dungeons_arise/lang/ru_ru.json` |
| [Let's Do] Vinery | 500 | `assets/vinery/lang/ru_ru.json` |
| Snow! Real Magic! | 74 | `assets/snowrealmagic/lang/ru_ru.json` |

Для Advanced Loot Info сохранены исходные идентификаторы ключей и форматные параметры вроде `%s`, `%1$s` и `%2$s`.

Переводы можно включать и отключать во вкладке **«Переводы и настройки»**. Изменения применяются сразу после перезагрузки ресурсов Minecraft. В списке наборов ресурсов отображается один общий пакет Rusify.

## Экспорт других модов

В меню переводов откройте **«Переводы и экспорт установленных модов»**. После сканирования используйте «Экспорт <100%». Файлы будут сохранены в `Rusify/exports`, отдельно в папки «Частичный перевод» и «Нет перевода».

## Сборка и обновление

В Windows запустите `build.bat`: сборщик найдёт JDK 21 или скачает Eclipse Temurin 21 с проверкой SHA-256, затем соберёт мод через Gradle Wrapper.

Для обновления исходников запустите `update-sources.bat`. Подходит Git-клон и папка, распакованная из ZIP; Git не требуется.

Подробности: [BUILDING.md](BUILDING.md).

## Лицензия

MIT. См. [LICENSE](LICENSE).
