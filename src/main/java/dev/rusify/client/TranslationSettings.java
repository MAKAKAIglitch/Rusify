package dev.rusify.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TranslationSettings {
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("rusify-client.json");
    private static boolean autoApplyBuiltInPacks = load();
    private static boolean builtInTranslationsInitialized = loadBuiltInTranslationsInitialized();

    private TranslationSettings() {
    }

    public static synchronized boolean autoApplyBuiltInPacks() {
        return autoApplyBuiltInPacks;
    }

    public static synchronized void setAutoApplyBuiltInPacks(boolean enabled) throws IOException {
        autoApplyBuiltInPacks = enabled;
        save();
    }

    public static synchronized boolean builtInTranslationsInitialized() {
        return builtInTranslationsInitialized;
    }

    public static synchronized void markBuiltInTranslationsInitialized() throws IOException {
        builtInTranslationsInitialized = true;
        save();
    }

    private static boolean load() {
        if (!Files.isRegularFile(CONFIG_FILE)) return false;
        try {
            JsonObject config = JsonParser.parseString(
                    Files.readString(CONFIG_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            return config.has("autoApplyBuiltInPacks")
                    && config.get("autoApplyBuiltInPacks").getAsBoolean();
        } catch (Exception exception) {
            System.err.println("[Rusify] Не удалось прочитать настройки перевода: " + exception.getMessage());
            return false;
        }
    }

    private static boolean loadBuiltInTranslationsInitialized() {
        if (!Files.isRegularFile(CONFIG_FILE)) return false;
        try {
            JsonObject config = JsonParser.parseString(
                    Files.readString(CONFIG_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            return config.has("builtInTranslationsInitialized")
                    && config.get("builtInTranslationsInitialized").getAsBoolean();
        } catch (Exception exception) {
            System.err.println("[Rusify] Не удалось прочитать состояние встроенных переводов: " + exception.getMessage());
            return false;
        }
    }

    private static void save() throws IOException {
        Files.createDirectories(CONFIG_FILE.getParent());
        JsonObject config = new JsonObject();
        config.addProperty("autoApplyBuiltInPacks", autoApplyBuiltInPacks);
        config.addProperty("builtInTranslationsInitialized", builtInTranslationsInitialized);
        Files.writeString(CONFIG_FILE,
                new GsonBuilder().setPrettyPrinting().create().toJson(config) + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }
}
