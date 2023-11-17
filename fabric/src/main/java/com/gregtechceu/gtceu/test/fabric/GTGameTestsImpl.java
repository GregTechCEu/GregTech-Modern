package com.gregtechceu.gtceu.test.fabric;

import com.gregtechceu.gtceu.test.GTGameTests;
import net.minecraft.gametest.framework.GameTestRegistry;

public class GTGameTestsImpl {

    public static void registerGameTests() {
        GTGameTests.registerTests(GameTestRegistry::register);
    }
}
