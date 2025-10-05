package com.gregtechceu.gtceu.api.mui.overlay;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiStatus.Experimental
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class OverlayManager {

    public static final List<OverlayHandler> overlays = new ArrayList<>();

    public static void register(OverlayHandler handler) {
        if (!overlays.contains(handler)) {
            overlays.add(handler);
            overlays.sort(OverlayHandler::compareTo);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onOpenScreen(ScreenEvent.Opening event) {
        if (event.getNewScreen() == event.getCurrentScreen()) return;
        OverlayStack.closeAll();
        for (OverlayHandler handler : overlays) {
            if (handler.isValidFor(event.getNewScreen())) {
                ModularScreen overlay = Objects.requireNonNull(handler.createOverlay(event.getNewScreen()),
                        "Overlays must not be null!");
                overlay.constructOverlay(event.getNewScreen());
                OverlayStack.open(overlay);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCloseScreen(ScreenEvent.Closing event) {
        OverlayStack.closeAll();
    }
}
