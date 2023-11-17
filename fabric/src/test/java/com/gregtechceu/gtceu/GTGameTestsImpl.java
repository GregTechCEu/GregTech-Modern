package com.gregtechceu.gtceu;

import net.fabricmc.fabric.impl.gametest.FabricGameTestHelper;
import net.minecraft.gametest.framework.GameTestRegistry;

public class GTGameTestsImpl {

    static {
        GTGameTests.registerTests(GameTestRegistry::register);
    }
}
