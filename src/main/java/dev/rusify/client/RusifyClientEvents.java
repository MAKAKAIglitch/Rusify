package dev.rusify.client;

import dev.rusify.Rusify;

import java.io.IOException;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Rusify.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class RusifyClientEvents {
    public static final KeyMapping OPEN_SCREEN = new KeyMapping(
            "key.rusify.open",
            GLFW.GLFW_KEY_O,
            "key.categories.rusify"
    );

    private RusifyClientEvents() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SCREEN);
    }

    @SubscribeEvent
    public static void registerModListScreen(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModList.get().getModContainerById(Rusify.MOD_ID).ifPresent(container -> {
            IConfigScreenFactory screenFactory = (modContainer, parent) -> new RusifyScreen(parent);
            container.registerExtensionPoint(IConfigScreenFactory.class, screenFactory);
        }));
    }
    @SubscribeEvent
    public static void applyBuiltInTranslations(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (!TranslationSettings.autoApplyBuiltInPacks()) return;
            try {
                TranslationPackManager.ensureDynamicPackEnabled();
            } catch (IOException exception) {
                System.err.println("[Rusify] Не удалось автоматически подключить встроенные переводы: " + exception.getMessage());
            }
        });
    }

}
