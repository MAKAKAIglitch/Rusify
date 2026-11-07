package dev.rusify.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public final class TranslationSettings {
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("rusify-client.json");
    private static boolean autoApplyBuiltInPacks = loadBoolean("autoApplyBuiltInPacks", true);
    private static boolean builtInTranslationsInitialized = loadBoolean("builtInTranslationsInitialized", false);
    private static Set<String> knownBuiltInTranslations = loadKnownBuiltInTranslations();

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

    public static synchronized Set<String> knownBuiltInTranslations() {
        return Set.copyOf(knownBuiltInTranslations);
    }

    public static synchronized void setKnownBuiltInTranslations(Set<String> modIds) throws IOException {
        knownBuiltInTranslations = new HashSet<>(modIds);
        save();
    }

    private static boolean loadBoolean(String key, boolean defaultValue) {
        if (!Files.isRegularFile(CONFIG_FILE)) return defaultValue;
        try {
            JsonObject config = JsonParser.parseString(
                    Files.readString(CONFIG_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            return config.has(key) ? config.get(key).getAsBoolean() : defaultValue;
        } catch (Exception exception) {
            System.err.println("[Rusify] Не удалось прочитать настройку " + key + ": " + exception.getMessage());
            return defaultValue;
        }
    }

    private static Set<String> loadKnownBuiltInTranslations() {
        Set<String> result = new HashSet<>();
        if (!Files.isRegularFile(CONFIG_FILE)) return result;
        try {
            JsonObject config = JsonParser.parseString(
                    Files.readString(CONFIG_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            if (config.has("knownBuiltInTranslations") && config.get("knownBuiltInTranslations").isJsonArray()) {
                for (var element : config.getAsJsonArray("knownBuiltInTranslations")) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                        result.add(element.getAsString());
                    }
                }
            }
        } catch (Exception exception) {
            System.err.println("[Rusify] Не удалось прочитать список встроенных переводов: " + exception.getMessage());
        }
        return result;
    }

    private static synchronized void save() throws IOException {
        Files.createDirectories(CONFIG_FILE.getParent());
        JsonObject config = new JsonObject();
        config.addProperty("autoApplyBuiltInPacks", autoApplyBuiltInPacks);
        config.addProperty("builtInTranslationsInitialized", builtInTranslationsInitialized);
        JsonArray known = new JsonArray();
        for (String modId : new TreeSet<>(knownBuiltInTranslations)) {
            known.add(modId);
        }
        config.add("knownBuiltInTranslations", known);
        Files.writeString(CONFIG_FILE,
                new GsonBuilder().setPrettyPrinting().create().toJson(config) + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }
}
