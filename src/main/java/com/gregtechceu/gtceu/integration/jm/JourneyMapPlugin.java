package com.gregtechceu.gtceu.integration.jm;

import com.gregtechceu.gtceu.api.GTValues;
import com.lowdragmc.lowdraglib.LDLib;
import lombok.Getter;
import net.minecraftforge.eventbus.api.IEventBus;

public class JourneyMapPlugin {
    @Getter
    private static boolean isActive = false;

    public static void init(IEventBus eventBus){
        if (LDLib.isModLoaded(GTValues.MODID_JOURNEY_MAP)) {
            isActive = true;

        }
    }
}
