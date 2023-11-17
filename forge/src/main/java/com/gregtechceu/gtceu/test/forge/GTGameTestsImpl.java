package com.gregtechceu.gtceu.test.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.test.GTGameTests;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GTGameTestsImpl {

    @SubscribeEvent
    public static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(GTGameTests.class);
    }
}
