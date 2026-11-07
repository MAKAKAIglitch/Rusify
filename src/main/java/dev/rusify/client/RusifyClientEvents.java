package dev.rusify.client;

import dev.rusify.Rusify;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
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
}
