package dev.rusify.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
                .bounds(center - 115, this.height / 2 - 28, 230, 24)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Экспорт языковых файлов"), button ->
                        Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(center - 115, this.height / 2 + 2, 230, 24)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Закрыть"), button -> onClose())
                .bounds(center - 50, this.height - 42, 100, 20)
                .build());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, "Rusify", width / 2, height / 2 - 76, 0xFFFFFF);
        graphics.drawCenteredString(font, "Инструменты русской локализации модов", width / 2,
                height / 2 - 55, 0xB8B8B8);
        graphics.drawCenteredString(font, "Управление готовыми переводами и экспорт строк модов.",
                width / 2, height / 2 + 42, 0xD0D0D0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
