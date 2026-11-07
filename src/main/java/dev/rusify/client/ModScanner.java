package dev.rusify.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ModScanner {
    private static final Pattern MOD_ID = Pattern.compile("(?m)^\\s*modId\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern DISPLAY_NAME = Pattern.compile("(?m)^\\s*displayName\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern LANG_PATH = Pattern.compile("^assets/([^/]+)/lang/(en_us|ru_ru)\\.json$");

    private ModScanner() {
    }

    public static List<ModScanResult> scan() throws IOException {
        Path modsDirectory = FMLPaths.MODSDIR.get();
        if (!Files.isDirectory(modsDirectory)) return List.of();

        List<Path> jars;
        try (var stream = Files.list(modsDirectory)) {
            jars = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .toList();
        }

        List<ModScanResult> results = new ArrayList<>();
        for (Path jarPath : jars) {
            try {
                ModScanResult result = scanJar(jarPath);
                if (result != null) results.add(result);
            } catch (IOException exception) {
                System.err.println("[Rusify] Не удалось просканировать " + jarPath.getFileName() + ": " + exception.getMessage());
            }
        }
        return results;
    }

    private static ModScanResult scanJar(Path path) throws IOException {
        try (JarFile jar = new JarFile(path.toFile())) {
            String toml = readText(jar, "META-INF/neoforge.mods.toml");
            if (toml == null) toml = readText(jar, "META-INF/mods.toml");

            String modId = firstMatch(MOD_ID, toml);
            String displayName = firstMatch(DISPLAY_NAME, toml);
            if (modId == null || modId.isBlank()) {
                modId = stripExtension(path.getFileName().toString()).toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
            }
            if (displayName == null || displayName.isBlank()) displayName = stripExtension(path.getFileName().toString());

            Map<String, String> english = new LinkedHashMap<>();
            Map<String, String> russian = new LinkedHashMap<>();
            Set<String> namespaces = new LinkedHashSet<>();
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                Matcher matcher = LANG_PATH.matcher(entry.getName());
                if (!matcher.matches() || entry.getSize() > 8 * 1024 * 1024) continue;

                String namespace = matcher.group(1);
                namespaces.add(namespace);
                String language = matcher.group(2);
                if (language.equals("en_us")) english.put(entry.getName(), readText(jar, entry));
                else russian.put(entry.getName(), readText(jar, entry));
            }

            if (english.isEmpty() && russian.isEmpty()) return null;

            Set<String> englishKeys = new LinkedHashSet<>();
            Set<String> russianKeys = new LinkedHashSet<>();
            english.forEach((entryName, text) -> addLocalizedKeys(englishKeys, entryName, text));
            russian.forEach((entryName, text) -> addLocalizedKeys(russianKeys, entryName, text));

            int translated = 0;
            for (String key : englishKeys) {
                if (russianKeys.contains(key)) translated++;
            }

            return new ModScanResult(
                    modId, displayName, path, List.copyOf(namespaces),
                    englishKeys.size(), translated, !russian.isEmpty(),
                    List.copyOf(english.keySet()), List.copyOf(russian.keySet())
            );
        }
    }

    public static Path extract(ModScanResult result) throws IOException {
        Path exportRoot = FMLPaths.GAMEDIR.get().resolve("Rusify").resolve("exports");
        String safeId = result.modId().replaceAll("[^a-zA-Z0-9_.-]", "_");
        Path modDirectory = exportRoot.resolve(safeId);
        Files.createDirectories(modDirectory);

        try (JarFile jar = new JarFile(result.jarPath().toFile())) {
            for (String entryName : result.englishEntries()) {
                Path destination = modDirectory.resolve(entryName).normalize();
                if (!destination.startsWith(modDirectory)) {
                    throw new IOException("Недопустимый путь внутри JAR: " + entryName);
                }
                Files.createDirectories(destination.getParent());
                try (InputStream input = jar.getInputStream(jar.getJarEntry(entryName))) {
                    Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        String metadata = "{\n"
                + "  \"mod_id\": \"" + jsonEscape(result.modId()) + "\",\n"
                + "  \"display_name\": \"" + jsonEscape(result.displayName()) + "\",\n"
                + "  \"source_jar\": \"" + jsonEscape(result.jarPath().getFileName().toString()) + "\",\n"
                + "  \"minecraft_target\": \"1.21.1\",\n"
                + "  \"source_language\": \"en_us\",\n"
                + "  \"target_language\": \"ru_ru\",\n"
                + "  \"english_keys\": " + result.englishKeys() + ",\n"
                + "  \"translated_keys\": " + result.translatedKeys() + "\n"
                + "}\n";
        Files.writeString(modDirectory.resolve("mod-info.json"), metadata, StandardCharsets.UTF_8);
        return modDirectory;
    }

    private static void addLocalizedKeys(Set<String> target, String entryName, String json) {
        Matcher pathMatcher = LANG_PATH.matcher(entryName);
        if (!pathMatcher.matches()) return;
        String namespace = pathMatcher.group(1);

        try {
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                        && !value.getAsString().isBlank()) {
                    target.add(namespace + ":" + entry.getKey());
                }
            }
        } catch (RuntimeException exception) {
            System.err.println("[Rusify] Некорректный lang JSON: " + entryName);
        }
    }

    private static String readText(JarFile jar, String entryName) throws IOException {
        JarEntry entry = jar.getJarEntry(entryName);
        return entry == null ? null : readText(jar, entry);
    }

    private static String readText(JarFile jar, JarEntry entry) throws IOException {
        try (InputStream input = jar.getInputStream(entry)) {
            return new String(input.readNBytes(8 * 1024 * 1024), StandardCharsets.UTF_8);
        }
    }

    private static String firstMatch(Pattern pattern, String input) {
        if (input == null) return null;
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
