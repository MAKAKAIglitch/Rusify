package dev.rusify.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
    private static final Pattern AUTHORS = Pattern.compile("(?m)^\\s*authors\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern DISPLAY_URL = Pattern.compile("(?m)^\\s*displayURL\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern ISSUES_URL = Pattern.compile("(?m)^\\s*issueTrackerURL\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern SOURCES_URL = Pattern.compile("(?m)^\\s*sources\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern LANG_PATH = Pattern.compile("^assets/([^/]+)/lang/(en_us|ru_ru)\\.json$");
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

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
            String authors = firstMatch(AUTHORS, toml);
            String repositoryUrl = firstValid(
                    safeRepositoryUrl(firstMatch(DISPLAY_URL, toml)),
                    safeHttpUrl(firstMatch(SOURCES_URL, toml)));
            String issuesUrl = safeHttpUrl(firstMatch(ISSUES_URL, toml));
            List<ModScanResult.AuthorLink> authorLinks = new ArrayList<>();

            // Fabric/Quilt metadata carries richer project and author contact links.
            String fabricMetadata = readText(jar, "fabric.mod.json");
            if (fabricMetadata == null) fabricMetadata = readText(jar, "quilt.mod.json");
            if (fabricMetadata != null) {
                try {
                    JsonObject metadata = JsonParser.parseString(fabricMetadata).getAsJsonObject();
                    JsonObject contact = metadata.has("contact") && metadata.get("contact").isJsonObject()
                            ? metadata.getAsJsonObject("contact") : new JsonObject();
                    repositoryUrl = firstValid(repositoryUrl, safeHttpUrl(jsonString(contact, "sources")));
                    issuesUrl = firstValid(issuesUrl, jsonString(contact, "issues"));

                    if (metadata.has("authors") && metadata.get("authors").isJsonArray()) {
                        List<String> authorNames = new ArrayList<>();
                        for (JsonElement author : metadata.getAsJsonArray("authors")) {
                            if (author.isJsonPrimitive()) {
                                authorNames.add(author.getAsString());
                            } else if (author.isJsonObject()) {
                                JsonObject person = author.getAsJsonObject();
                                String name = jsonString(person, "name");
                                if (name == null || name.isBlank()) continue;
                                authorNames.add(name);
                                JsonObject authorContact = person.has("contact") && person.get("contact").isJsonObject()
                                        ? person.getAsJsonObject("contact") : new JsonObject();
                                String authorUrl = firstValid(
                                        jsonString(authorContact, "github"),
                                        jsonString(authorContact, "homepage"),
                                        jsonString(authorContact, "url"));
                                if (authorUrl != null) authorLinks.add(new ModScanResult.AuthorLink(name, authorUrl));
                            }
                        }
                        if (!authorNames.isEmpty()) authors = String.join(", ", authorNames);
                    }
                } catch (RuntimeException ignored) {
                    System.err.println("[Rusify] Некорректный mod metadata JSON: " + path.getFileName());
                }
            }
            boolean hasModMetadata = modId != null && !modId.isBlank();
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

            if (!hasModMetadata && english.isEmpty() && russian.isEmpty()) return null;

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
                    List.copyOf(english.keySet()), List.copyOf(russian.keySet()),
                    authors == null || authors.isBlank() ? "Не указаны в метаданных" : authors,
                    repositoryUrl, issuesUrl, List.copyOf(authorLinks)
            );
        }
    }

    /**
     * Exports source language files and creates editable Russian templates.
     * Existing non-empty Russian translations are retained; missing values are empty strings.
     */
    public static Path extract(ModScanResult result) throws IOException {
        if (result.englishEntries().isEmpty()) {
            throw new IOException("В этом моде нет en_us.json для подготовки перевода.");
        }
        if (result.englishKeys() <= 0) {
            throw new IOException("В этом моде нет строк en_us.json для перевода.");
        }
        if (result.translatedKeys() >= result.englishKeys()) {
            throw new IOException("Русский перевод этого мода уже полный.");
        }

        Path exportRoot = FMLPaths.GAMEDIR.get().resolve("Rusify").resolve("exports");
        String category = result.translatedKeys() > 0 ? "Частичный перевод" : "Нет перевода";
        Path categoryDirectory = exportRoot.resolve(category).normalize();
        String safeId = result.modId().replaceAll("[^a-zA-Z0-9_.-]", "_");
        Path modDirectory = categoryDirectory.resolve(safeId).normalize();
        if (!modDirectory.startsWith(categoryDirectory)) {
            throw new IOException("Недопустимый идентификатор мода: " + result.modId());
        }
        Files.createDirectories(modDirectory);

        try (JarFile jar = new JarFile(result.jarPath().toFile())) {
            // Keep the original source files and any Russian-only language files in the export.
            List<String> originalEntries = new ArrayList<>(result.englishEntries());
            originalEntries.addAll(result.russianEntries());
            for (String entryName : originalEntries) {
                Path destination = safeResolve(modDirectory, entryName);
                Files.createDirectories(destination.getParent());
                JarEntry sourceEntry = jar.getJarEntry(entryName);
                if (sourceEntry == null) continue;
                try (InputStream input = jar.getInputStream(sourceEntry)) {
                    Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Build one ru_ru.json for every en_us.json, including namespaces with no Russian file.
            for (String englishEntryName : result.englishEntries()) {
                String russianEntryName = englishEntryName.replace("/en_us.json", "/ru_ru.json");
                JarEntry englishEntry = jar.getJarEntry(englishEntryName);
                if (englishEntry == null) continue;

                JsonObject source = readJsonObject(jar, englishEntry);
                JarEntry russianEntry = jar.getJarEntry(russianEntryName);
                JsonObject existingRussian = russianEntry == null
                        ? new JsonObject()
                        : readJsonObject(jar, russianEntry);

                JsonObject template = new JsonObject();
                for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
                    JsonElement current = existingRussian.get(entry.getKey());
                    if (isNonEmptyString(current)) {
                        template.add(entry.getKey(), current.deepCopy());
                    } else if (entry.getValue().isJsonPrimitive()
                            && entry.getValue().getAsJsonPrimitive().isString()) {
                        template.add(entry.getKey(), new JsonPrimitive(""));
                    } else {
                        template.add(entry.getKey(), entry.getValue().deepCopy());
                    }
                }

                // Keep valid Russian-only keys too; they may be used by the mod at runtime.
                for (Map.Entry<String, JsonElement> entry : existingRussian.entrySet()) {
                    if (!template.has(entry.getKey())) {
                        template.add(entry.getKey(), entry.getValue().deepCopy());
                    }
                }

                Path destination = safeResolve(modDirectory, russianEntryName);
                Files.createDirectories(destination.getParent());
                Files.writeString(destination, PRETTY_GSON.toJson(template) + System.lineSeparator(),
                        StandardCharsets.UTF_8);
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
                + "  \"translated_keys\": " + result.translatedKeys() + ",\n"
                + "  \"missing_keys\": " + result.missingKeys() + ",\n"
                + "  \"coverage_percent\": " + result.coveragePercent() + "\n"
                + "}\n";
        Files.writeString(modDirectory.resolve("mod-info.json"), metadata, StandardCharsets.UTF_8);
        return modDirectory;
    }

    private static Path safeResolve(Path root, String entryName) throws IOException {
        Path destination = root.resolve(entryName).normalize();
        if (!destination.startsWith(root.normalize())) {
            throw new IOException("Недопустимый путь внутри JAR: " + entryName);
        }
        return destination;
    }

    private static JsonObject readJsonObject(JarFile jar, JarEntry entry) throws IOException {
        try (InputStream input = jar.getInputStream(entry)) {
            String json = new String(input.readNBytes(8 * 1024 * 1024), StandardCharsets.UTF_8);
            try {
                JsonElement parsed = JsonParser.parseString(json);
                if (!parsed.isJsonObject()) throw new IOException("Ожидался JSON-объект: " + entry.getName());
                return parsed.getAsJsonObject();
            } catch (RuntimeException exception) {
                throw new IOException("Некорректный JSON в " + entry.getName(), exception);
            }
        }
    }

    private static boolean isNonEmptyString(JsonElement value) {
        return value != null && value.isJsonPrimitive()
                && value.getAsJsonPrimitive().isString() && !value.getAsString().isBlank();
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

    private static String jsonString(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) return null;
        try {
            return object.get(key).getAsString();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String firstValid(String... values) {
        for (String value : values) {
            String valid = safeHttpUrl(value);
            if (valid != null) return valid;
        }
        return null;
    }

    private static String safeRepositoryUrl(String value) {
        String url = safeHttpUrl(value);
        if (url == null) return null;
        try {
            java.net.URI uri = java.net.URI.create(url);
            String host = uri.getHost().toLowerCase(java.util.Locale.ROOT);
            boolean knownGitHost = host.equals("github.com") || host.equals("www.github.com")
                    || host.equals("gitlab.com") || host.equals("www.gitlab.com")
                    || host.equals("codeberg.org") || host.equals("www.codeberg.org");
            String path = uri.getPath();
            return knownGitHost && path != null && path.split("/").length >= 3 ? url : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String safeHttpUrl(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            java.net.URI uri = java.net.URI.create(value.trim());
            String scheme = uri.getScheme();
            if (uri.getHost() == null || scheme == null
                    || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))) return null;
            return uri.toASCIIString();
        } catch (IllegalArgumentException ignored) {
            return null;
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
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
