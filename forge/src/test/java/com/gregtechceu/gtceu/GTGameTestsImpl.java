package com.gregtechceu.gtceu;

import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(GTCEu.MOD_ID)
public class GTGameTestsImpl {

    @SubscribeEvent
    public void registerTests(RegisterGameTestsEvent event) {
        GTGameTests.registerTests(event::register);
    }

}
