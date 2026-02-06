package com.gregtechceu.gtceu.api.mui.overlay;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class OverlayManager {

    public static final List<OverlayHandler> overlays = new ArrayList<>();

    public static void register(OverlayHandler handler) {
        if (!overlays.contains(handler)) {
            overlays.add(handler);
            overlays.sort(OverlayHandler::compareTo);
        }
    }
}
