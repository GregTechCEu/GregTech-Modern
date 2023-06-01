package com.gregtechceu.gtceu.common.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.world.level.block.Block;

/**
 * @author KilaBash
 * @date 2023/3/27
 * @implNote CommonProxyImpl
 */
public class CommonProxyImpl {
    public static void onKubeJSSetup() {
        CommonProxy.init();
    }

    public static void init() {
        if (!GTCEu.isKubeJSLoaded()) {
            CommonProxy.init();
        }
    }
}
