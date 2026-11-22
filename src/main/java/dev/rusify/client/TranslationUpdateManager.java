package dev.rusify.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TranslationUpdateManager {
    private static final String TREE_URL =
            "https://api.github.com/repos/MAKAKAIglitch/Rusify/git/trees/main?recursive=1";
    private static final String RAW_BASE =
            "https://raw.githubusercontent.com/MAKAKAIglitch/Rusify/main/";
    private static final int MAX_JSON_BYTES = 4 * 1024 * 1024;
    private static final Path ROOT = FMLPaths.GAMEDIR.get().resolve("Rusify").resolve("updates");
    private static final Path PENDING = ROOT.resolve("pending");
    private static final Path ACTIVE = ROOT.resolve("active");
    private static final AtomicBoolean RUNNING = new AtomicBoolean();

    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Rusify-translation-updater");
        thread.setDaemon(true);
        return thread;
    });
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private TranslationUpdateManager() {
    }

    static Path activeFile(String modId) {
        return ACTIVE.resolve(modId).resolve("ru_ru.json");
    }

    public static void applyPendingUpdates() throws IOException {
        if (!Files.isDirectory(PENDING)) return;
        int applied = 0;
        for (String modId : TranslationPackManager.builtInTranslationIdsInOrder()) {
            Path staged = PENDING.resolve(modId + ".json");
            Path checksum = PENDING.resolve(modId + ".sha1");
            if (!Files.isRegularFile(staged) || !Files.isRegularFile(checksum)) continue;
            try {
                byte[] bytes;
                try (InputStream input = Files.newInputStream(staged)) {
                    bytes = readBounded(input);
                }
                String expectedSha = Files.readString(checksum, StandardCharsets.UTF_8).trim();
                if (!isValidTranslation(bytes) || !gitBlobSha(bytes).equalsIgnoreCase(expectedSha)) {
                    System.err.println("[Rusify] Отложенный перевод " + modId
                            + " не прошёл проверку; сохранена предыдущая версия.");
                    Files.deleteIfExists(staged);
                    Files.deleteIfExists(checksum);
                    continue;
                }
                writeAtomic(activeFile(modId), bytes);
                Files.deleteIfExists(staged);
                Files.deleteIfExists(checksum);
                applied++;
            } catch (IOException | RuntimeException exception) {
                System.err.println("[Rusify] Не удалось применить обновление " + modId + ": "
                        + exception.getMessage());
            }
        }
        if (applied > 0) {
            System.out.println("[Rusify] Подготовлено обновлений переводов: " + applied
                    + ". Они загружены до начала игровой сессии.");
        }
    }

    public static void checkForUpdates() {
        if (!TranslationSettings.shouldCheckTranslationUpdates()
                || !RUNNING.compareAndSet(false, true)) return;
        WORKER.execute(() -> {
            try {
                int staged = checkRemoteTranslations();
                if (staged > 0) {
                    System.out.println("[Rusify] Найдены обновления переводов: " + staged
                            + ". Они будут применены при следующем запуске Minecraft.");
                }
            } catch (Exception exception) {
                System.err.println("[Rusify] Проверка обновлений переводов не удалась; "
                        + "текущие переводы сохранены: " + exception.getMessage());
            } finally {
                RUNNING.set(false);
            }
        });
    }

    private static int checkRemoteTranslations() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(TREE_URL))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Rusify-Translation-Updater")
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .GET().build();
        HttpResponse<String> response = HTTP.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("GitHub вернул HTTP " + response.statusCode());
        }
        if (response.body().length() > 8 * 1024 * 1024) {
            throw new IOException("Список файлов GitHub превысил допустимый размер.");
        }
        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        if (root.has("truncated") && root.get("truncated").getAsBoolean()) {
            throw new IOException("GitHub вернул неполный список файлов.");
        }
        TranslationSettings.markTranslationUpdateCheck();

        Map<String, String> remoteFiles = new HashMap<>();
        for (JsonElement element : root.getAsJsonArray("tree")) {
            if (!element.isJsonObject()) continue;
            JsonObject entry = element.getAsJsonObject();
            if (!entry.has("type") || !"blob".equals(entry.get("type").getAsString())) continue;
            String path = entry.get("path").getAsString();
            if (path.startsWith("src/main/resources/data/rusify/translations/")
                    && path.endsWith("/ru_ru.json") && entry.has("sha")) {
                remoteFiles.put(path, entry.get("sha").getAsString());
            }
        }

        Files.createDirectories(PENDING);
        int staged = 0;
        for (String modId : TranslationPackManager.builtInTranslationIdsInOrder()) {
            if (!TranslationSettings.autoUpdateTranslations()) break;
            try {
                String resourceFolder = TranslationPackManager.translationResourceFolder(modId);
                String sourcePath = "src/main/resources/data/rusify/translations/"
                        + resourceFolder + "/ru_ru.json";
                String remoteSha = remoteFiles.get(sourcePath);
                // A missing upstream file is not a deletion instruction: keep the last known-good translation.
                if (remoteSha == null) continue;

                byte[] current;
                try (InputStream input = TranslationPackManager.openTranslationStream(modId)) {
                    current = readBounded(input);
                }
                if (gitBlobSha(current).equalsIgnoreCase(remoteSha)) {
                    Files.deleteIfExists(PENDING.resolve(modId + ".json"));
                    Files.deleteIfExists(PENDING.resolve(modId + ".sha1"));
                    continue;
                }

                Path pendingChecksum = PENDING.resolve(modId + ".sha1");
                if (Files.isRegularFile(pendingChecksum)
                        && Files.readString(pendingChecksum, StandardCharsets.UTF_8).trim()
                        .equalsIgnoreCase(remoteSha)
                        && Files.isRegularFile(PENDING.resolve(modId + ".json"))) continue;

                byte[] downloaded = downloadTranslation(sourcePath);
                if (!isValidTranslation(downloaded) || !gitBlobSha(downloaded).equalsIgnoreCase(remoteSha)) {
                    System.err.println("[Rusify] Обновление " + modId
                            + " не прошло проверку целостности; старая версия сохранена.");
                    continue;
                }
                writeAtomic(PENDING.resolve(modId + ".json"), downloaded);
                writeAtomic(pendingChecksum, remoteSha.getBytes(StandardCharsets.UTF_8));
                staged++;
            } catch (IOException | RuntimeException exception) {
                System.err.println("[Rusify] Не удалось обновить " + modId
                        + "; предыдущий перевод сохранён: " + exception.getMessage());
            }
        }
        return staged;
    }

    private static byte[] downloadTranslation(String sourcePath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(RAW_BASE + sourcePath))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Rusify-Translation-Updater")
                .GET().build();
        HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream body = response.body()) {
            if (response.statusCode() != 200) {
                throw new IOException("GitHub не отдал " + sourcePath + " (HTTP "
                        + response.statusCode() + ")");
            }
            return readBounded(body);
        }
    }

    private static byte[] readBounded(InputStream input) throws IOException {
        byte[] bytes = input.readNBytes(MAX_JSON_BYTES + 1);
        if (bytes.length > MAX_JSON_BYTES) throw new IOException("Файл перевода превысил лимит 4 МиБ.");
        return bytes;
    }

    private static boolean isValidTranslation(byte[] bytes) {
        try {
            JsonElement parsed = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
            if (!parsed.isJsonObject() || parsed.getAsJsonObject().size() == 0) return false;
            for (var entry : parsed.getAsJsonObject().entrySet()) {
                if (entry.getKey().isBlank() || !entry.getValue().isJsonPrimitive()
                        || !entry.getValue().getAsJsonPrimitive().isString()
                        || entry.getValue().getAsString().isBlank()) return false;
            }
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static String gitBlobSha(byte[] bytes) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(("blob " + bytes.length + "\0").getBytes(StandardCharsets.UTF_8));
            digest.update(bytes);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException("SHA-1 недоступен в этой Java-среде.", exception);
        }
    }

    private static void writeAtomic(Path target, byte[] bytes) throws IOException {
        Files.createDirectories(target.getParent());
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(temporary, bytes);
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
