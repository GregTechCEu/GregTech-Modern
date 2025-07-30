package com.gregtechceu.gtceu.integration.ae2;

import com.gregtechceu.gtceu.api.capability.compat.FeCompat;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.api.config.PowerUnits;

public class AE2Compat {

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(AE2Compat.class);
        setEUConversionRatio();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sync(ModConfigEvent.Reloading event) {
        setEUConversionRatio();
    }

    private static void setEUConversionRatio() {
        // Conversion ratio of EU to AE = EU to FE * FE to AE
        double FEtoAE = PowerUnits.RF.conversionRatio;
        double EUtoFE = FeCompat.ratio(false);
        PowerUnits.valueOf("EU").conversionRatio = EUtoFE * FEtoAE;
    }
}
