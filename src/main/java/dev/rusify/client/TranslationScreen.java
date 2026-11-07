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
        try {
            carryOnEnabled = TranslationPackManager.isCarryOnEnabled();
        } catch (IOException exception) {
            message = "Не удалось прочитать активные ресурс-паки: " + exception.getMessage();
        }

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Carry On: " + (carryOnEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleCarryOn())
                .bounds(center - 145, 88, 290, 24).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Автоподключение готовых переводов: "
                                + (TranslationSettings.autoApplyBuiltInPacks() ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleAutoApply())
                .bounds(center - 145, 118, 290, 24).build());

        addRenderableWidget(Button.builder(Component.literal("Экспортировать перевод Carry On"),
                        button -> exportCarryOn())
                .bounds(center - 145, 153, 290, 24).build());

        addRenderableWidget(Button.builder(Component.literal("Переводы и экспорт установленных модов"),
                        button -> Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(center - 145, 183, 290, 24).build());

        addRenderableWidget(Button.builder(Component.literal("Назад"), button -> onClose())
                .bounds(center - 50, height - 32, 100, 20).build());
    }

    private void toggleCarryOn() {
        try {
            boolean enable = !TranslationPackManager.isCarryOnEnabled();
            TranslationPackManager.setCarryOnEnabled(enable);
            reopen(enable ? "Русский перевод Carry On включён." : "Русский перевод Carry On выключен.");
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

    private void exportCarryOn() {
        try {
            Path path = TranslationPackManager.exportCarryOnPack();
            reopen("Ресурс-пак Carry On подготовлен: " + path);
        } catch (IOException exception) {
            reopen("Ошибка экспорта Carry On: " + exception.getMessage());
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

        graphics.drawCenteredString(font, "Переводы Rusify", width / 2,  thirty(), 0xFFFFFF);
        graphics.drawCenteredString(font, "Готовые переводы работают как отдельный ресурс-пак.",
                width / 2, 54, 0xB8B8B8);
        graphics.drawCenteredString(font, "Carry On: русский перевод из официального репозитория автора.",
                width / 2, 68, 0x77DD88);
        graphics.drawCenteredString(font, "Автоподключение применяется при следующем запуске Minecraft.",
                width / 2, 218, 0xA0A0A0);
        graphics.drawCenteredString(font, trim(message, 110), width / 2, 237, 0xD0D0D0);
    }

    private int thirty() {
        return  thirtyValue();
    }

    private int thirtyValue() {
        return  thirtyConstant();
    }

    private int thirtyConstant() {
        return 30;
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
