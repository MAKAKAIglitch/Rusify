package dev.rusify.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;

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
                "sable", "tectonic", "expandeddelight", "screenshot_viewer", "ragdoll_reactions", "lendersdelight", "held_item_info", "kiwi", "pingwheel", "sable_player_ragdoll", "aci", "ichunutil", "resourcefulconfig", "betterzoom", "create_more_automation", "create_trimmed", "jumpoverfences", "breakingplace211neo", "trimmed", "curios_for_ammo_box", "betteradvancements", "rebindnarrator", "betterbiomereblend", "ragdoll_corpse", "dungeons_arise", "curios", "everycomp", "distanthorizons", "artifacts", "sodium_extra", "jei", "createnuclear", "vinery", "snowrealmagic", "furniture", "farm_and_charm", "puffish_skills", "moredelight", "moreculling"};
        String[] names = {"Carry On", "OfflineLAN", "Advanced Loot Info", "Stone Zone", "Gems Realm", "Punchy!",
                "Sable", "Tectonic", "Expanded Delight", "Screenshot Viewer", "Ragdoll Reactions", "L_Ender's Delight", "Held Item Info", "Kiwi Library", "Ping Wheel", "Sable Ragdolls", "AdvancedCoreInfo", "iChunUtil", "Resourceful Config", "Better Zoom", "Create: More Automation", "Create: Trimmed", "Jump Over Fences", "BreakingPlace 211 neo", "Trimmed", "Curios for Ammo Box", "Better Advancements", "RebindNarrator", "Better Biome ReBlend", "Sable: Ragdoll Corpse", "When Dungeons Arise", "Curios", "Every Compat", "Distant Horizons", "Artifacts", "Sodium Extra", "Just Enough Items (JEI)", "Create Nuclear", "[Let's Do] Vinery", "Snow! Real Magic!", "[Let's Do] Furniture", "[Let's Do] Farm & Charm", "Pufferfish's Skills", "More Delight", "More Culling"};
        int buttonWidth = Math.min(190, (width - 40 - 8) / 2);
        int totalWidth = buttonWidth * 2 + 8;
        for (int i = 0; i < modIds.length; i++) {
            int column = i % 2;
            int row = i / 2;
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
                    .bounds(center - totalWidth / 2 + column * (buttonWidth + 8), 60 + row * 20, buttonWidth, 18).build());
        }
        boolean allEnabled = false;
        try {
            allEnabled = TranslationPackManager.areAllBuiltInTranslationsEnabled();
        } catch (IOException exception) {
            message = "Не удалось проверить состояние переводов: " + exception.getMessage();
        }
        final boolean enableAll = !allEnabled;
        addRenderableWidget(Button.builder(
                        Component.literal(allEnabled ? "Отключить все переводы" : "Включить все переводы"),
                        button -> toggleAllTranslations(enableAll))
                .bounds(center - 145, 60 + ((modIds.length + 1) / 2) * 20 + 6, 290, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Переводы и экспорт установленных модов"),
                        button -> Minecraft.getInstance().setScreen(new ExportScreen(this)))
                .bounds(center - 145, 60 + ((modIds.length + 1) / 2) * 20 + 28, 290, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), button -> onClose())
                .bounds(center - 50, height - 28, 100, 20).build());
    }

    private void toggleAllTranslations(boolean enabled) {
        try {
            TranslationPackManager.setAllBuiltInTranslationsEnabled(enabled);
            reopen(enabled ? "Все встроенные переводы включены." : "Все встроенные переводы выключены.");
        } catch (IOException exception) {
            reopen("Не удалось переключить все переводы: " + exception.getMessage());
        }
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
                case "held_item_info" -> "Held Item Info";
                case "kiwi" -> "Kiwi Library";
                case "pingwheel" -> "Ping Wheel";
                case "sable_player_ragdoll" -> "Sable Ragdolls";
                case "aci" -> "AdvancedCoreInfo";
                case "ichunutil" -> "iChunUtil";
                case "resourcefulconfig" -> "Resourceful Config";
                case "betterzoom" -> "Better Zoom";
                case "create_more_automation" -> "Create: More Automation";
                case "create_trimmed" -> "Create: Trimmed";
                case "jumpoverfences" -> "Jump Over Fences";
                case "breakingplace211neo" -> "BreakingPlace 211 neo";
                case "trimmed" -> "Trimmed";
                case "curios_for_ammo_box" -> "Curios for Ammo Box";
                case "betteradvancements" -> "Better Advancements";
                case "rebindnarrator" -> "RebindNarrator";
                case "betterbiomereblend" -> "Better Biome ReBlend";
                case "ragdoll_corpse" -> "Sable: Ragdoll Corpse";
                case "dungeons_arise" -> "When Dungeons Arise";
                case "curios" -> "Curios";
                case "everycomp" -> "Every Compat";
                case "distanthorizons" -> "Distant Horizons";
                case "artifacts" -> "Artifacts";
                case "sodium_extra" -> "Sodium Extra";
                case "jei" -> "Just Enough Items (JEI)";
                case "createnuclear" -> "Create Nuclear";
                case "vinery" -> "[Let's Do] Vinery";
                case "snowrealmagic" -> "Snow! Real Magic!";
                case "furniture" -> "[Let's Do] Furniture";
                case "farm_and_charm" -> "[Let's Do] Farm & Charm";
                case "puffish_skills" -> "Pufferfish's Skills";
                case "moredelight" -> "More Delight";
                case "moreculling" -> "More Culling";
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
