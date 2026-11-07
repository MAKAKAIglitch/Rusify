package dev.rusify.client;

import java.nio.file.Path;
import java.util.List;

public record ModScanResult(
        String modId,
        String displayName,
        Path jarPath,
        List<String> namespaces,
        int englishKeys,
        int translatedKeys,
        boolean hasRussianFile,
        List<String> englishEntries,
        List<String> russianEntries
) {
    public int missingKeys() {
        return Math.max(0, englishKeys - translatedKeys);
    }

    public int coveragePercent() {
        if (englishKeys == 0) return 0;
        return Math.min(100, Math.round(translatedKeys * 100.0f / englishKeys));
    }

    public String status() {
        if (englishKeys == 0) return "Нет en_us.json";
        if (!hasRussianFile) return "Нет русского";
        if (translatedKeys >= englishKeys) return "Перевод найден";
        return "Частичный перевод";
    }
}
