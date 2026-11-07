package dev.rusify.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Path;

public final class TranslationScreen extends Screen {
    private final Screen parent;
    private String message;

    public TranslationScreen(Screen parent) {
        this(parent, "Выберите готовый перевод или откройте экспорт модов.");
    }

    private TranslationScreen(Screen parent, String message) {
        super(Component.literal("Rusify — переводы"));
        this.parent = parent;
        this.message = message;
    }

    @Override
    protected void init() {
        int center = width / 2;
        boolean carryOnEnabled = false;
        boolean offlineLanEnabled = false;
        boolean aliEnabled = false;
        try {
            carryOnEnabled = TranslationPackManager.isCarryOnEnabled();
            offlineLanEnabled = TranslationPackManager.isOfflineLanEnabled();
            aliEnabled = TranslationPackManager.isAdvancedLootInfoEnabled();
        } catch (IOException exception) {
            message = "Не удалось прочитать активные ресурс-паки: " + exception.getMessage();
        }

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Carry On: " + (carryOnEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("carryon"))
                .bounds(center - 145, 78, 290, 22).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Carry On"),
                        button -> exportTranslation("carryon", "Carry On"))
                .bounds(center - 145, 104, 290, 22).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод OfflineLAN: " + (offlineLanEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("offlinelan"))
                .bounds(center - 145, 130, 290, 22).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод OfflineLAN"),
                        button -> exportTranslation("offlinelan", "OfflineLAN"))
                .bounds(center - 145, 156, 290, 22).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Advanced Loot Info: " + (aliEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("ali"))
                .bounds(center - 145, 182, 290, 22).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Advanced Loot Info"),
                        button -> exportTranslation("ali", "Advanced Loot Info"))
                .bounds(center - 145, 208, 290, 22).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Автоподключение всех переводов: "
                                + (TranslationSettings.autoApplyBuiltInPacks() ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleAutoApply())
                .bounds(center - 145, 234, 290, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Переводы и экспорт установленных модов"),
                        button -> Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(center - 145, 260, 290, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Назад"), button -> onClose())
                .bounds(center - 50, height - 28, 100, 20).build());
    }

    private void toggleTranslation(String modId) {
        try {
            boolean enabled = !TranslationPackManager.isTranslationEnabled(modId);
            TranslationPackManager.setTranslationEnabled(modId, enabled);
            String name = switch (modId) {
                case "carryon" -> "Carry On";
                case "offlinelan" -> "OfflineLAN";
                case "ali" -> "Advanced Loot Info";
                default -> modId;
            };
            reopen("Перевод " + name + (enabled ? " включён." : " выключен."));
        } catch (IOException exception) {
            reopen("Не удалось переключить перевод: " + exception.getMessage());
        }
    }

    private void toggleAutoApply() {
        try {
            boolean enabled = !TranslationSettings.autoApplyBuiltInPacks();
            TranslationSettings.setAutoApplyBuiltInPacks(enabled);
            reopen(enabled
                    ? "Готовые переводы будут подключаться при следующем запуске игры."
                    : "Автоподключение выключено.");
        } catch (IOException exception) {
            reopen("Не удалось сохранить настройки: " + exception.getMessage());
        }
    }

    private void exportTranslation(String modId, String name) {
        try {
            Path path = TranslationPackManager.exportTranslationPack(modId);
            reopen("Ресурс-пак " + name + " подготовлен: " + path);
        } catch (IOException exception) {
            reopen("Ошибка экспорта " + name + ": " + exception.getMessage());
        }
    }

    private void reopen(String nextMessage) {
        Minecraft.getInstance().setScreen(new TranslationScreen(parent, nextMessage));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, "Переводы Rusify", width / 2, 24, 0xFFFFFF);
        graphics.drawCenteredString(font, "Встроенные переводы работают офлайн как ресурс-пак.", width / 2,
                46, 0xB8B8B8);
        graphics.drawCenteredString(font, "Автоподключение применяется при следующем запуске Minecraft.",
                width / 2, 238, 0xA0A0A0);
        graphics.drawCenteredString(font, trim(message, 110), width / 2, 254, 0xD0D0D0);
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
