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
        String[] modIds = {"carryon", "offlinelan", "ali", "stonezone", "gemsrealm", "punchy",
                "sable", "tectonic", "expandeddelight", "screenshot_viewer", "ragdoll_reactions", "lendersdelight"};
        String[] names = {"Carry On", "OfflineLAN", "Advanced Loot Info", "Stone Zone", "Gems Realm", "Punchy!",
                "Sable", "Tectonic", "Expanded Delight", "Screenshot Viewer", "Ragdoll Reactions", "L_Ender's Delight"};
        for (int i = 0; i < modIds.length; i++) {
            String modId = modIds[i];
            boolean enabled = false;
            try {
                enabled = TranslationPackManager.isTranslationEnabled(modId);
            } catch (IOException exception) {
                message = "Не удалось прочитать состояние переводов: " + exception.getMessage();
            }
            addRenderableWidget(Button.builder(
                            Component.literal(names[i] + ": " + (enabled ? "ВКЛ" : "ВЫКЛ")),
                            button -> toggleTranslation(modId))
                    .bounds(center - 145, 65 + i * 25, 290, 21).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Переводы и экспорт установленных модов"),
                        button -> Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(center - 145, 65 + modIds.length * 25 + 8, 290, 21).build());
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
                case "stonezone" -> "Stone Zone";
                case "gemsrealm" -> "Gems Realm";
                case "punchy" -> "Punchy!";
                case "sable" -> "Sable";
                case "tectonic" -> "Tectonic";
                case "expandeddelight" -> "Expanded Delight";
                case "screenshot_viewer" -> "Screenshot Viewer";
                case "ragdoll_reactions" -> "Ragdoll Reactions";
                case "lendersdelight" -> "L_Ender's Delight";
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
        graphics.drawCenteredString(font, "Все включённые переводы находятся в одном динамическом ресурс-паке.",
                width / 2, 43, 0xB8B8B8);
        graphics.drawCenteredString(font, "При выключении перевод удаляется из общего пакета.",
                width / 2, height - 48, 0xA0A0A0);
        graphics.drawCenteredString(font, trim(message, 110), width / 2, height - 35, 0xD0D0D0);
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
