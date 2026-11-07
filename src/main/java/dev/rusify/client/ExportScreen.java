package dev.rusify.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ExportScreen extends Screen {
    private static final int ROW_HEIGHT = 25;
    private final Screen parent;
    private final List<ModScanResult> results = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private String message = "Нажмите «Сканировать», чтобы проверить папку mods.";
    private boolean scanning = false;
    private boolean exporting = false;

    public ExportScreen() {
        this(null);
    }

    public ExportScreen(Screen parent) {
        super(Component.literal("Rusify — экспорт"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Сканировать"), button -> scan())
                .bounds(this.width / 2 - 156, 12, 92, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Извлечь выбранный"), button -> extractSelected())
                .bounds(this.width / 2 - 60, 12, 112, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Экспорт <100%"), button -> exportIncomplete())
                .bounds(this.width / 2 + 56, 12, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Информация о моде"), button -> openSelectedInfo())
                .bounds(100, this.height - 34, 140, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), button -> onClose())
                .bounds(12, this.height - 34, 80, 20).build());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private void openSelectedInfo() {
        if (selectedIndex < 0 || selectedIndex >= results.size()) {
            message = "Сначала выберите мод в списке.";
            return;
        }
        Minecraft.getInstance().setScreen(new ModInfoScreen(this, results.get(selectedIndex)));
    }

    private void scan() {
        if (scanning || exporting) return;
        scanning = true;
        message = "Сканирование JAR-файлов...";
        CompletableFuture.supplyAsync(() -> {
            try {
                return ModScanner.scan();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }).whenComplete((found, error) -> Minecraft.getInstance().execute(() -> {
            scanning = false;
            if (error != null) {
                Throwable cause = error.getCause() == null ? error : error.getCause();
                message = "Ошибка сканирования: " + cause.getMessage();
                return;
            }
            results.clear();
            results.addAll(found);
            selectedIndex = results.isEmpty() ? -1 : 0;
            scrollOffset = 0;
            long untranslated = results.stream()
                    .filter(result -> result.englishKeys() > 0 && result.translatedKeys() == 0).count();
            long rusify = results.stream()
                    .filter(result -> TranslationPackManager.hasBuiltInTranslation(result.modId())).count();
            long partial = results.stream()
                    .filter(result -> !TranslationPackManager.hasBuiltInTranslation(result.modId())
                            && result.englishKeys() > 0
                            && result.translatedKeys() > 0
                            && result.translatedKeys() < result.englishKeys()).count();
            message = "Модов: " + results.size()
                    + " | перевод Rusify: " + rusify
                    + " | частичный: " + partial
                    + " | нет: " + untranslated;
        }));
    }

    private void extractSelected() {
        if (exporting) return;
        if (selectedIndex < 0 || selectedIndex >= results.size()) {
            message = "Сначала выберите мод в списке.";
            return;
        }
        try {
            var directory = ModScanner.extract(results.get(selectedIndex));
            message = "Экспортировано: " + results.get(selectedIndex).displayName() + " → " + directory;
        } catch (IOException exception) {
            message = "Ошибка экспорта: " + exception.getMessage();
        }
    }

    private void exportIncomplete() {
        if (scanning || exporting) return;
        List<ModScanResult> incomplete = results.stream()
                .filter(result -> result.englishKeys() > 0 && result.coveragePercent() < 100)
                .toList();
        if (incomplete.isEmpty()) {
            message = "Неполных переводов не найдено. Сначала нажмите «Сканировать».";
            return;
        }

        exporting = true;
        message = "Экспорт неполных переводов: 0 из " + incomplete.size() + "...";
        CompletableFuture.supplyAsync(() -> {
            int rusifySuccess = 0;
            int partialSuccess = 0;
            int untranslatedSuccess = 0;
            List<String> failures = new ArrayList<>();
            for (ModScanResult result : incomplete) {
                try {
                    ModScanner.extract(result);
                    if (TranslationPackManager.hasBuiltInTranslation(result.modId())) rusifySuccess++;
                    else if (result.translatedKeys() > 0) partialSuccess++;
                    else untranslatedSuccess++;
                } catch (IOException exception) {
                    failures.add(result.displayName() + ": " + exception.getMessage());
                }
            }
            return new ExportSummary(rusifySuccess, partialSuccess, untranslatedSuccess, incomplete.size(), failures);
        }).whenComplete((summary, error) -> Minecraft.getInstance().execute(() -> {
            exporting = false;
            if (error != null) {
                Throwable cause = error.getCause() == null ? error : error.getCause();
                message = "Ошибка экспорта: " + cause.getMessage();
                return;
            }
            message = "Экспорт: Rusify — " + summary.rusifySuccess()
                    + ", частичный — " + summary.partialSuccess()
                    + ", без перевода — " + summary.untranslatedSuccess()
                    + ". Папки: Rusify/exports";
            if (!summary.failures().isEmpty()) {
                message += " | Ошибок: " + summary.failures().size()
                        + " (подробности в latest.log)";
                summary.failures().forEach(failure -> System.err.println("[Rusify] Ошибка экспорта: " + failure));
            }
        }));
    }

    private record ExportSummary(int rusifySuccess, int partialSuccess, int untranslatedSuccess,
                                  int total, List<String> failures) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, "Rusify", width / 2, 42, 0xFFFFFF);
        graphics.drawString(font, "Мод", 18, 62, 0xA0A0A0);
        graphics.drawString(font, "Статус русского", width - 190, 62, 0xA0A0A0);

        int listTop = 76;
        int listBottom = height - 66;
        int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
        int end = Math.min(results.size(), scrollOffset + visibleRows);

        for (int index = scrollOffset; index < end; index++) {
            ModScanResult result = results.get(index);
            int y = listTop + (index - scrollOffset) * ROW_HEIGHT;
            boolean selected = index == selectedIndex;
            int background = selected ? 0x804A7198 : 0x60303030;
            graphics.fill(12, y - 3, width - 12, y + ROW_HEIGHT - 4, background);
            graphics.drawString(font, trim(result.displayName(), 30), 20, y + 3, 0xFFFFFF);
            boolean hasRusifyTranslation = TranslationPackManager.hasBuiltInTranslation(result.modId());
            String status = hasRusifyTranslation ? "Есть перевод Rusify" : result.status();
            if (result.englishKeys() > 0) status += " (" + result.coveragePercent() + "% в моде)";
            graphics.drawString(font, trim(status, 30), width - 190, y + 3,
                    hasRusifyTranslation ? 0x77DD88
                            : (!result.hasRussianFile() ? 0xFF7777
                            : (result.coveragePercent() < 100 ? 0xFFCC66 : 0x77DD88)));
        }

        if (results.isEmpty()) {
            graphics.drawCenteredString(font, scanning ? "Сканирование..." : "Список пока пуст", width / 2, height / 2, 0xB0B0B0);
        }
        graphics.drawString(font, trim(message, 105), 12, height - 52, 0xD0D0D0);
        graphics.drawString(font, "Папки: «Есть перевод Rusify», «Частичный перевод», «Нет перевода».",
                12, height - 14, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int listTop = 76;
        int listBottom = height - 66;
        if (mouseY >= listTop && mouseY < listBottom && mouseX >= 12 && mouseX <= width - 12) {
            int row = (int) ((mouseY - listTop) / ROW_HEIGHT);
            int index = scrollOffset + row;
            if (index >= 0 && index < results.size()) {
                selectedIndex = index;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxRows = Math.max(1, (height - 142) / ROW_HEIGHT);
        int maxOffset = Math.max(0, results.size() - maxRows);
        scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(verticalAmount)));
        return true;
    }

    private static String trim(String value, int max) {
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
