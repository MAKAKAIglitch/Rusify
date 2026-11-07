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
        addRenderableWidget(Button.builder(Component.literal("Экспорт языковых файлов"), button ->
                        Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(center - 105, this.height / 2 - 12, 210, 28)
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
        graphics.drawCenteredString(font, "Rusify", width / 2, height / 2 - 70, 0xFFFFFF);
        graphics.drawCenteredString(font, "Инструменты русской локализации модов", width / 2,
                height / 2 - 49, 0xB8B8B8);
        graphics.drawCenteredString(font, "Сейчас доступен поиск и экспорт неполных переводов.",
                width / 2, height / 2 + 34, 0xD0D0D0);
        graphics.drawCenteredString(font,
                "Автоперевод планируется позже как резервный вариант после локальных переводов и кэша.",
                width / 2, height / 2 + 48, 0x999999);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
