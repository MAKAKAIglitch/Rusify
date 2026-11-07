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

public final class RusifyScreen extends Screen {
    private static final int ROW_HEIGHT = 25;
    private final List<ModScanResult> results = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private String message = "Нажмите «Сканировать», чтобы проверить папку mods.";
    private boolean scanning = false;

    public RusifyScreen() {
        super(Component.literal("Rusify"));
    }

    @Override
    protected void init() {
        int buttonY = this.height - 34;
        addRenderableWidget(Button.builder(Component.literal("Сканировать"), button -> scan())
                .bounds(this.width / 2 - 156, 12, 92, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Извлечь выбранный"), button -> extractSelected())
                .bounds(this.width / 2 - 60, 12, 112, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Извлечь без RU"), button -> extractMissing())
                .bounds(this.width / 2 + 56, 12, 100, 20).build());
    }

    private void scan() {
        if (scanning) return;
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
                message = "Ошибка сканирования: " + error.getCause().getMessage();
                return;
            }
            results.clear();
            results.addAll(found);
            selectedIndex = results.isEmpty() ? -1 : 0;
            scrollOffset = 0;
            long missing = results.stream().filter(result -> !result.hasRussianFile()).count();
            message = "Найдено модов с языковыми файлами: " + results.size()
                    + " | без ru_ru.json: " + missing;
        }));
    }

    private void extractSelected() {
        if (selectedIndex < 0 || selectedIndex >= results.size()) {
            message = "Сначала выберите мод в списке.";
            return;
        }
        extract(results.get(selectedIndex));
    }

    private void extractMissing() {
        List<ModScanResult> missing = results.stream()
                .filter(result -> !result.hasRussianFile() && result.englishKeys() > 0)
                .toList();
        if (missing.isEmpty()) {
            message = "Модов без ru_ru.json не найдено.";
            return;
        }
        int success = 0;
        for (ModScanResult result : missing) {
            try {
                ModScanner.extract(result);
                success++;
            } catch (IOException exception) {
                message = "Ошибка извлечения " + result.displayName() + ": " + exception.getMessage();
            }
        }
        message = "Извлечено модов: " + success + " из " + missing.size()
                + ". Папка: Rusify/exports";
    }

    private void extract(ModScanResult result) {
        try {
            var directory = ModScanner.extract(result);
            message = "Извлечено: " + result.displayName() + " → " + directory;
        } catch (IOException exception) {
            message = "Ошибка извлечения: " + exception.getMessage();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, "Rusify", width / 2, 42, 0xFFFFFF);
        graphics.drawString(font, "Мод", 18, 62, 0xA0A0A0);
        graphics.drawString(font, "Статус русского", width - 150, 62, 0xA0A0A0);

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
            String status = result.status();
            if (result.englishKeys() > 0) status += " (" + result.coveragePercent() + "%)";
            graphics.drawString(font, trim(status, 24), width - 150, y + 3,
                    !result.hasRussianFile() ? 0xFF7777 : (result.coveragePercent() < 100 ? 0xFFCC66 : 0x77DD88));
        }

        if (results.isEmpty()) {
            graphics.drawCenteredString(font, scanning ? "Сканирование..." : "Список пока пуст", width / 2, height / 2, 0xB0B0B0);
        }
        graphics.drawString(font, trim(message, 105), 12, height - 52, 0xD0D0D0);
        graphics.drawString(font, "Выберите мод в списке. Извлечение сохраняет исходные JAR без изменений.",
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
