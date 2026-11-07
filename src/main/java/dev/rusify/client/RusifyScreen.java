package dev.rusify.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RusifyScreen extends Screen {
    private final Screen parent;

    public RusifyScreen() {
        this(null);
    }

    public RusifyScreen(Screen parent) {
        super(Component.literal("Rusify"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        addRenderableWidget(Button.builder(Component.literal("Переводы и настройки"), button ->
                        Minecraft.getInstance().setScreen(new TranslationScreen(this)))
                .bounds(center - 115, this.height / 2 - 43, 230, 24)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Экспорт языковых файлов"), button ->
                        Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(center - 115, this.height / 2 - 13, 230, 24)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Открыть папку экспортов"), button -> openExportsFolder())
                .bounds(center - 115, this.height / 2 + 17, 230, 24)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Закрыть"), button -> onClose())
                .bounds(center - 50, this.height - 42, 100, 20)
                .build());
    }

    private void openExportsFolder() {
        Path exports = FMLPaths.GAMEDIR.get().resolve("Rusify").resolve("exports");
        try {
            Files.createDirectories(exports);
            Util.getPlatform().openFile(exports.toFile());
        } catch (IOException | RuntimeException exception) {
            System.err.println("[Rusify] Не удалось открыть папку экспортов: " + exception.getMessage());
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, "Rusify", width / 2, height / 2 - 88, 0xFFFFFF);
        graphics.drawCenteredString(font, "Инструменты русской локализации модов", width / 2,
                height / 2 - 67, 0xB8B8B8);
        graphics.drawCenteredString(font, "Управление готовыми переводами и экспорт строк модов.",
                width / 2, height / 2 + 51, 0xD0D0D0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
