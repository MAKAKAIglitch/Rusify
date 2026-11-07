package dev.rusify.client;

import dev.rusify.Rusify;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Rusify.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class RusifyClientForgeEvents {
    private RusifyClientForgeEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (RusifyClientEvents.OPEN_SCREEN.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new RusifyScreen());
            }
        }
    }
}
