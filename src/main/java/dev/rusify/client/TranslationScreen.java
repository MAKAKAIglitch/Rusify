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
        boolean stoneZoneEnabled = false;
        boolean gemsRealmEnabled = false;
        boolean punchyEnabled = false;
        boolean sableEnabled = false;
        boolean tectonicEnabled = false;
        boolean expandedDelightEnabled = false;
        boolean screenshotViewerEnabled = false;
        boolean ragdollReactionsEnabled = false;
        try {
            carryOnEnabled = TranslationPackManager.isCarryOnEnabled();
            offlineLanEnabled = TranslationPackManager.isOfflineLanEnabled();
            aliEnabled = TranslationPackManager.isAdvancedLootInfoEnabled();
            stoneZoneEnabled = TranslationPackManager.isStoneZoneEnabled();
            gemsRealmEnabled = TranslationPackManager.isGemsRealmEnabled();
            punchyEnabled = TranslationPackManager.isPunchyEnabled();
            sableEnabled = TranslationPackManager.isSableEnabled();
            tectonicEnabled = TranslationPackManager.isTranslationEnabled("tectonic");
            expandedDelightEnabled = TranslationPackManager.isExpandedDelightEnabled();
            screenshotViewerEnabled = TranslationPackManager.isScreenshotViewerEnabled();
            ragdollReactionsEnabled = TranslationPackManager.isRagdollReactionsEnabled();
        } catch (IOException exception) {
            message = "Не удалось прочитать активные ресурс-паки: " + exception.getMessage();
        }

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Carry On: " + (carryOnEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("carryon"))
                .bounds(center - 145, 58, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Carry On"),
                        button -> exportTranslation("carryon", "Carry On"))
                .bounds(center - 145, 80, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод OfflineLAN: " + (offlineLanEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("offlinelan"))
                .bounds(center - 145, 102, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод OfflineLAN"),
                        button -> exportTranslation("offlinelan", "OfflineLAN"))
                .bounds(center - 145, 124, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Advanced Loot Info: " + (aliEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("ali"))
                .bounds(center - 145, 146, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Advanced Loot Info"),
                        button -> exportTranslation("ali", "Advanced Loot Info"))
                .bounds(center - 145, 168, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Stone Zone: " + (stoneZoneEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("stonezone"))
                .bounds(center - 145, 190, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Stone Zone"),
                        button -> exportTranslation("stonezone", "Stone Zone"))
                .bounds(center - 145, 212, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Gems Realm: " + (gemsRealmEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("gemsrealm"))
                .bounds(center - 145, 234, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Gems Realm"),
                        button -> exportTranslation("gemsrealm", "Gems Realm"))
                .bounds(center - 145, 256, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Punchy!: " + (punchyEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("punchy"))
                .bounds(center - 145, 278, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Punchy!"),
                        button -> exportTranslation("punchy", "Punchy!"))
                .bounds(center - 145, 300, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Sable: " + (sableEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("sable"))
                .bounds(center - 145, 322, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Sable"),
                        button -> exportTranslation("sable", "Sable"))
                .bounds(center - 145, 344, 290, 20).build());


        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Tectonic: " + (tectonicEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("tectonic"))
                .bounds(center - 145, 366, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Tectonic"),
                        button -> exportTranslation("tectonic", "Tectonic"))
                .bounds(center - 145, 388, 290, 20).build());


        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Expanded Delight: " + (expandedDelightEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("expandeddelight"))
                .bounds(center - 145, 410, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Expanded Delight"),
                        button -> exportTranslation("expandeddelight", "Expanded Delight"))
                .bounds(center - 145, 432, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Screenshot Viewer: " + (screenshotViewerEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("screenshot_viewer"))
                .bounds(center - 145, 454, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Экспортировать перевод Screenshot Viewer"),
                        button -> exportTranslation("screenshot_viewer", "Screenshot Viewer"))
                .bounds(center - 145, 476, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Перевод Ragdoll Reactions: " + (ragdollReactionsEnabled ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleTranslation("ragdoll_reactions"))
                .bounds(center - 145, 498, 290, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("Автоподключение всех переводов: "
                                + (TranslationSettings.autoApplyBuiltInPacks() ? "ВКЛ" : "ВЫКЛ")),
                        button -> toggleAutoApply())
                .bounds(center - 145, 520, 290, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Переводы и экспорт установленных модов"),
                        button -> Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(center - 145, 543, 290, 20).build());

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
        graphics.drawCenteredString(font, "Автоподключение применяется при запуске игры.",
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
