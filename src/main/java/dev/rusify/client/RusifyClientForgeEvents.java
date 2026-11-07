package dev.rusify.client;

import com.mojang.brigadier.Command;
import dev.rusify.Rusify;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Rusify.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class RusifyClientForgeEvents {
    private static boolean startupPackHandled;

    private RusifyClientForgeEvents() {
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("rusify")
                .executes(context -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.setScreen(new RusifyScreen(minecraft.screen));
                    return Command.SINGLE_SUCCESS;
                }));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!startupPackHandled) {
            startupPackHandled = true;
            if (!TranslationSettings.builtInTranslationsInitialized()) {
                try {
                    TranslationPackManager.setAllBuiltInTranslationsEnabled(true);
                    TranslationSettings.markBuiltInTranslationsInitialized();
                } catch (IOException exception) {
                    System.err.println("[Rusify] Не удалось автоматически подключить переводы: "
                            + exception.getMessage());
                }
            }
        }

        while (RusifyClientEvents.OPEN_SCREEN.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new RusifyScreen(null));
            }
        }
    }
}
