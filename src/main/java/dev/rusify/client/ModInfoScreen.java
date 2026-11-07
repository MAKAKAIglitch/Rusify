package dev.rusify.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class ModInfoScreen extends Screen {
    private final Screen parent;
    private final ModScanResult mod;
    private final List<String> infoLines = new ArrayList<>();

    public ModInfoScreen(Screen parent, ModScanResult mod) {
        super(Component.literal("Rusify — информация о моде"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        int center = width / 2;
        int y = 105;
        if (mod.homepageUrl() != null) {
            addRenderableWidget(linkButton("Сайт проекта", mod.homepageUrl(), center - 154, y));
            y += 25;
        }
        if (mod.sourcesUrl() != null) {
            addRenderableWidget(linkButton("Исходный код / репозиторий", mod.sourcesUrl(), center - 154, y));
            y += 25;
        }
        if (mod.issuesUrl() != null) {
            addRenderableWidget(linkButton("Ошибки и предложения", mod.issuesUrl(), center - 154, y));
            y += 25;
        }
        for (ModScanResult.AuthorLink author : mod.authorLinks().stream().limit(4).toList()) {
            addRenderableWidget(linkButton("Автор: " + author.name(), author.url(), center - 154, y));
            y += 25;
        }
        addRenderableWidget(Button.builder(Component.literal("Назад"), button -> Minecraft.getInstance().setScreen(parent))
                .bounds(center - 154, height - 30, 308, 20).build());
    }

    private Button linkButton(String label, String url, int x, int y) {
        return Button.builder(Component.literal(label), button -> {
            try {
                Util.getPlatform().openUri(URI.create(url));
            } catch (RuntimeException exception) {
                System.err.println("[Rusify] Не удалось открыть ссылку: " + exception.getMessage());
            }
        }).bounds(x, y, 308, 20).build();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, mod.displayName(), width / 2, 18, 0xFFFFFF);
        graphics.drawCenteredString(font, "ID: " + mod.modId(), width / 2, 34, 0xA0A0A0);
        graphics.drawString(font, "Авторы: " + mod.authors(), 18, 54, 0xFFFFFF);
        graphics.drawString(font, "Файл: " + mod.jarPath().getFileName(), 18, 70, 0xA0A0A0);
        graphics.drawString(font, "Языковые пространства: " + String.join(", ", mod.namespaces()), 18, 84, 0xA0A0A0);
        if (mod.homepageUrl() == null && mod.sourcesUrl() == null && mod.issuesUrl() == null
                && mod.authorLinks().isEmpty()) {
            graphics.drawCenteredString(font, "Ссылки не указаны в метаданных этого мода.", width / 2, 115, 0xFFCC66);
        }
        graphics.drawCenteredString(font, "Ссылки берутся из metadata самого мода; Rusify их не угадывает.",
                width / 2, height - 43, 0x888888);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
