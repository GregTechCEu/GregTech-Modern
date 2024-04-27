package com.gregtechceu.gtceu.test.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.test.GTGameTests;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = GTCEu.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class GTGameTestsImpl {

    @SubscribeEvent
    public static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(GTGameTests.class);
    }
}
