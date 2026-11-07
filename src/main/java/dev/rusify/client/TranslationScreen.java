package dev.rusify.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public final class TranslationScreen extends Screen {
    private static final String[] MOD_IDS = {
            "carryon", "offlinelan", "ali", "stonezone", "gemsrealm", "punchy", "sable", "tectonic",
            "expandeddelight", "screenshot_viewer", "ragdoll_reactions", "lendersdelight", "held_item_info",
            "kiwi", "pingwheel", "sable_player_ragdoll", "aci", "ichunutil", "resourcefulconfig", "betterzoom",
            "create_more_automation", "create_trimmed", "jumpoverfences", "breakingplace211neo", "trimmed",
            "curios_for_ammo_box", "betteradvancements", "rebindnarrator", "betterbiomereblend", "ragdoll_corpse",
            "dungeons_arise", "curios", "everycomp", "distanthorizons", "artifacts", "sodium_extra", "jei",
            "createnuclear", "vinery", "snowrealmagic", "furniture", "farm_and_charm", "puffish_skills",
            "moredelight", "moreculling", "golemoverhaul", "fastquit", "create_hypertube", "create_optical",
            "create_deep_dark", "copperequipment", "chunkloaders"
    };

    private static final String[] NAMES = {
            "Carry On", "OfflineLAN", "Advanced Loot Info", "Stone Zone", "Gems Realm", "Punchy!", "Sable",
            "Tectonic", "Expanded Delight", "Screenshot Viewer", "Ragdoll Reactions", "L_Ender's Delight",
            "Held Item Info", "Kiwi Library", "Ping Wheel", "Sable Ragdolls", "AdvancedCoreInfo", "iChunUtil",
            "Resourceful Config", "Better Zoom", "Create: More Automation", "Create: Trimmed", "Jump Over Fences",
            "BreakingPlace 211 neo", "Trimmed", "Curios for Ammo Box", "Better Advancements", "RebindNarrator",
            "Better Biome ReBlend", "Sable: Ragdoll Corpse", "When Dungeons Arise", "Curios", "Every Compat",
            "Distant Horizons", "Artifacts", "Sodium Extra", "Just Enough Items (JEI)", "Create Nuclear",
            "[Let's Do] Vinery", "Snow! Real Magic!", "[Let's Do] Furniture", "[Let's Do] Farm & Charm",
            "Pufferfish's Skills", "More Delight", "More Culling", "Golem Overhaul", "FastQuit",
            "Create: Hypertubes", "Create: Optical", "Create: Deep Dark", "Copper Equipment", "Chunk Loaders"
    };

    private static final int ROW_HEIGHT = 23;
    private final Screen parent;
    private String message;
    private double scrollOffset;

    public TranslationScreen(Screen parent) {
        this(parent, "Выберите готовый перевод или откройте экспорт модов.", 0);
    }

    private TranslationScreen(Screen parent, String message, double scrollOffset) {
        super(Component.literal("Rusify — переводы"));
        this.parent = parent;
        this.message = message;
        this.scrollOffset = scrollOffset;
    }

    private int listTop() {
        return 66;
    }

    private int listBottom() {
        return Math.max(listTop() + 24, height - 100);
    }

    private int columns() {
        return width >= 420 ? 2 : 1;
    }

    private int listLeft() {
        return 12;
    }

    private int listRight() {
        return width - 12;
    }

    private int columnGap() {
        return 8;
    }

    private int cellWidth() {
        return Math.max(1, (listRight() - listLeft() - (columns() - 1) * columnGap()) / columns());
    }

    private int rowCount() {
        return (MOD_IDS.length + columns() - 1) / columns();
    }

    private double maxScroll() {
        return Math.max(0, rowCount() * ROW_HEIGHT - (listBottom() - listTop()));
    }

    private void clampScroll() {
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll()));
    }

    @Override
    protected void init() {
        clampScroll();
        int center = width / 2;
        int buttonWidth = Math.min(145, Math.max(100, (width - 28) / 2));
        int gap = 8;
        int total = buttonWidth * 2 + gap;
        int left = center - total / 2;

        addRenderableWidget(Button.builder(Component.literal("Включить все"), button -> toggleAllTranslations(true))
                .bounds(left, height - 76, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Отключить все"), button -> toggleAllTranslations(false))
                .bounds(left + buttonWidth + gap, height - 76, buttonWidth, 20).build());

        int exportWidth = Math.min(200, Math.max(100, width - 124));
        int footerWidth = exportWidth + 100;
        int footerLeft = center - footerWidth / 2;
        addRenderableWidget(Button.builder(Component.literal("Экспорт установленных модов"),
                        button -> Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(footerLeft, height - 49, exportWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), button -> onClose())
                .bounds(footerLeft + exportWidth + 8, height - 49, 92, 20).build());
    }

    private void toggleAllTranslations(boolean enabled) {
        try {
            TranslationPackManager.setAllBuiltInTranslationsEnabled(enabled);
            reopen(enabled ? "Все встроенные переводы включены." : "Все встроенные переводы выключены.");
        } catch (IOException exception) {
            reopen("Не удалось переключить переводы: " + exception.getMessage());
        }
    }

    private void toggleTranslation(String modId) {
        try {
            boolean enabled = !TranslationPackManager.isTranslationEnabled(modId);
            TranslationPackManager.setTranslationEnabled(modId, enabled);
            String name = nameFor(modId);
            reopen("Перевод " + name + (enabled ? " включён." : " выключен."));
        } catch (IOException exception) {
            reopen("Не удалось переключить перевод: " + exception.getMessage());
        }
    }

    private static String nameFor(String modId) {
        for (int i = 0; i < MOD_IDS.length; i++) {
            if (MOD_IDS[i].equals(modId)) return NAMES[i];
        }
        return modId;
    }

    private void reopen(String nextMessage) {
        Minecraft.getInstance().setScreen(new TranslationScreen(parent, nextMessage, scrollOffset));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= listTop() && mouseY < listBottom()
                && mouseX >= listLeft() && mouseX < listRight()) {
            int relativeX = (int) mouseX - listLeft();
            int stride = cellWidth() + columnGap();
            int column = Math.min(columns() - 1, relativeX / stride);
            int withinColumn = relativeX - column * stride;
            if (withinColumn < cellWidth()) {
                int row = (int) ((mouseY - listTop() + scrollOffset) / ROW_HEIGHT);
                int index = row * columns() + column;
                if (index >= 0 && index < MOD_IDS.length) {
                    toggleTranslation(MOD_IDS[index]);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= listLeft() && mouseX < listRight()
                && mouseY >= listTop() && mouseY < listBottom()) {
            scrollOffset -= scrollY * ROW_HEIGHT * 2.5;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, "Переводы Rusify", width / 2, 22, 0xFFFFFF);
        graphics.drawCenteredString(font, "Нажмите на перевод, чтобы включить или выключить его", width / 2,
                42, 0xB8B8B8);
        graphics.drawCenteredString(font, "Колёсико мыши — прокрутка списка", width / 2, 54, 0xA0A0A0);

        int left = listLeft();
        int top = listTop();
        int right = listRight();
        int bottom = listBottom();
        int cellWidth = cellWidth();
        int cols = columns();

        graphics.enableScissor(left, top, right, bottom);
        for (int i = 0; i < MOD_IDS.length; i++) {
            int column = i % cols;
            int row = i / cols;
            int x = left + column * (cellWidth + columnGap());
            int y = top + row * ROW_HEIGHT - (int) scrollOffset;
            boolean hovered = mouseX >= x && mouseX < x + cellWidth
                    && mouseY >= y && mouseY < y + ROW_HEIGHT - 3;
            boolean enabled = false;
            try {
                enabled = TranslationPackManager.isTranslationEnabled(MOD_IDS[i]);
            } catch (IOException exception) {
                message = "Ошибка чтения состояния: " + exception.getMessage();
            }

            int background = hovered ? 0xFF454545 : 0xFF292929;
            graphics.fill(x, y, x + cellWidth, y + ROW_HEIGHT - 3, background);
            graphics.fill(x, y, x + 2, y + ROW_HEIGHT - 3, enabled ? 0xFF55C878 : 0xFF777777);
            String label = NAMES[i] + (enabled ? "  ✓" : "  —");
            String visibleLabel = font.plainSubstrByWidth(label, Math.max(12, cellWidth - 14));
            graphics.drawCenteredString(font, visibleLabel, x + cellWidth / 2, y + 5,
                    enabled ? 0xFFFFFF : 0xB8B8B8);
        }
        graphics.disableScissor();

        graphics.drawCenteredString(font, trim(message, 90), width / 2, height - 88, 0xD0D0D0);
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
