package dev.rusify.client;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TranslationPackManager {
    private static final String LEGACY_PACK_ID = "file/Rusify-Built-in-Translations";
    private static final String OWNERSHIP_MARKER = ".rusify-pack";

    private TranslationPackManager() {
    }

    public static boolean isCarryOnEnabled() throws IOException {
        return isTranslationEnabled("carryon");
    }

    public static boolean isOfflineLanEnabled() throws IOException {
        return isTranslationEnabled("offlinelan");
    }

    public static boolean isAdvancedLootInfoEnabled() throws IOException {
        return isTranslationEnabled("ali");
    }

    public static boolean isStoneZoneEnabled() throws IOException {
        return isTranslationEnabled("stonezone");
    }

    public static boolean isGemsRealmEnabled() throws IOException {
        return isTranslationEnabled("gemsrealm");
    }

    public static boolean isPunchyEnabled() throws IOException {
        return isTranslationEnabled("punchy");
    }

    public static boolean isTranslationEnabled(String modId) throws IOException {
        Translation translation = translation(modId);
        return selectedPacks(Minecraft.getInstance()).contains(translation.packId());
    }

    public static Path exportCarryOnPack() throws IOException {
        return exportTranslationPack("carryon");
    }

    public static Path exportOfflineLanPack() throws IOException {
        return exportTranslationPack("offlinelan");
    }

    public static Path exportAdvancedLootInfoPack() throws IOException {
        return exportTranslationPack("ali");
    }

    public static Path exportStoneZonePack() throws IOException {
        return exportTranslationPack("stonezone");
    }

    public static Path exportGemsRealmPack() throws IOException {
        return exportTranslationPack("gemsrealm");
    }

    public static Path exportPunchyPack() throws IOException {
        return exportTranslationPack("punchy");
    }

    public static Path exportTranslationPack(String modId) throws IOException {
        Translation translation = translation(modId);
        Path packDirectory = packDirectory(translation);
        preparePack(packDirectory, translation);
        return packDirectory;
    }

    public static void setCarryOnEnabled(boolean enabled) throws IOException {
        setTranslationEnabled("carryon", enabled);
    }

    public static void setOfflineLanEnabled(boolean enabled) throws IOException {
        setTranslationEnabled("offlinelan", enabled);
    }

    public static void setAdvancedLootInfoEnabled(boolean enabled) throws IOException {
        setTranslationEnabled("ali", enabled);
    }

    public static void setStoneZoneEnabled(boolean enabled) throws IOException {
        setTranslationEnabled("stonezone", enabled);
    }

    public static void setGemsRealmEnabled(boolean enabled) throws IOException {
        setTranslationEnabled("gemsrealm", enabled);
    }

    public static void setPunchyEnabled(boolean enabled) throws IOException {
        setTranslationEnabled("punchy", enabled);
    }

    public static void setTranslationEnabled(String modId, boolean enabled) throws IOException {
        Translation translation = translation(modId);
        Minecraft minecraft = Minecraft.getInstance();
        preparePack(packDirectory(translation), translation);

        List<String> selected = selectedPacks(minecraft);
        // Remove the combined pack created by older Rusify builds during migration.
        selected.removeIf(LEGACY_PACK_ID::equals);
        if (enabled) {
            if (!selected.contains(translation.packId())) selected.add(translation.packId());
        } else {
            selected.removeIf(translation.packId()::equals);
        }

        minecraft.options.save();
        minecraft.reloadResourcePacks();
    }

    public static void setAllBuiltInTranslationsEnabled(boolean enabled) throws IOException {
        Minecraft minecraft = Minecraft.getInstance();
        List<String> selected = selectedPacks(minecraft);
        selected.removeIf(LEGACY_PACK_ID::equals);

        for (String modId : List.of("carryon", "offlinelan", "ali", "stonezone", "gemsrealm", "punchy")) {
            Translation translation = translation(modId);
            preparePack(packDirectory(translation), translation);
            if (enabled) {
                if (!selected.contains(translation.packId())) selected.add(translation.packId());
            } else {
                selected.removeIf(translation.packId()::equals);
            }
        }

        minecraft.options.save();
        minecraft.reloadResourcePacks();
    }

    private static Translation translation(String modId) throws IOException {
        return switch (modId) {
            case "carryon" -> new Translation("carryon", "Carry On", "carryon");
            case "offlinelan" -> new Translation("offlinelan", "OfflineLAN", "offlinelan");
            case "ali" -> new Translation("ali", "Advanced Loot Info", "ali");
            case "stonezone" -> new Translation("stonezone", "Stone Zone", "stonezone");
            case "gemsrealm" -> new Translation("gemsrealm", "Gems Realm", "gemsrealm");
            case "punchy" -> new Translation("punchy", "Punchy!", "punchy");
            default -> throw new IOException("Неизвестный встроенный перевод: " + modId);
        };
    }

    private static List<String> selectedPacks(Minecraft minecraft) throws IOException {
        try {
            Field field = minecraft.options.getClass().getDeclaredField("resourcePacks");
            field.setAccessible(true);
            Object value = field.get(minecraft.options);
            if (value instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<String> selected = (List<String>) list;
                return selected;
            }
            throw new IOException("Список активных ресурс-паков имеет неизвестный формат.");
        } catch (ReflectiveOperationException | SecurityException exception) {
            throw new IOException("Не удалось получить список активных ресурс-паков Minecraft.", exception);
        }
    }

    private static Path packDirectory(Translation translation) {
        return net.neoforged.fml.loading.FMLPaths.GAMEDIR.get()
                .resolve("resourcepacks").resolve(translation.packFolder());
    }

    private static void preparePack(Path packDirectory, Translation translation) throws IOException {
        Files.createDirectories(packDirectory.getParent());
        Path marker = packDirectory.resolve(OWNERSHIP_MARKER);
        if (Files.exists(packDirectory) && !Files.isRegularFile(marker)) {
            throw new IOException("Папка " + packDirectory.getFileName()
                    + " уже существует и не принадлежит Rusify. Она не была изменена.");
        }

        Files.createDirectories(packDirectory.resolve("assets/" + translation.namespace() + "/lang"));
        Files.writeString(packDirectory.resolve("pack.mcmeta"),
                "{\n  \"pack\": {\n    \"pack_format\": 34,\n"
                        + "    \"description\": \"Rusify — " + translation.displayName()
                        + " (русский перевод)\"\n  }\n}\n",
                StandardCharsets.UTF_8);
        Files.writeString(marker, "Generated by Rusify. Safe to regenerate.\n", StandardCharsets.UTF_8);

        String resource = "/data/rusify/translations/" + translation.resourceFolder() + "/ru_ru.json";
        try (InputStream input = TranslationPackManager.class.getResourceAsStream(resource)) {
            if (input == null) throw new IOException("Встроенный перевод " + translation.displayName()
                    + " не найден в JAR Rusify.");
            Files.copy(input,
                    packDirectory.resolve("assets/" + translation.namespace() + "/lang/ru_ru.json"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record Translation(String modId, String displayName, String namespace, String resourceFolder) {
        private Translation(String modId, String displayName, String namespace) {
            this(modId, displayName, namespace, modId);
        }

        private String packFolder() {
            return "Rusify-" + modId + "-Translation";
        }

        private String packId() {
            return "file/" + packFolder();
        }
    }
}
