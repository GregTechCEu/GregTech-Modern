package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.gui.CoverUIFactory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.api.gui.MachineUIFactory;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryObjectBuilderTypes;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;

public class CommonProxy {

    /**
     * If kjs is loaded, make sure our mod is loaded after it. {@link com.gregtechceu.gtceu.core.mixins.kjs.KubeJSMixin}
     */
    @ExpectPlatform
    public static void onKubeJSSetup() {
        throw new AssertionError();
    }

    public static void init() {
        GTCEu.LOGGER.info("GTCEu common proxy init!");
        ConfigHolder.init();
        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        GTPlacerTypes.init();
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        GTElements.init();
        MaterialIconSet.init();
        GTMaterials.init();
        GTSoundEntries.init();
        GTCovers.init();
        GTFluids.init();
        GTBlocks.init();
        GTBlockEntities.init();
        GTRecipeTypes.init();
        GTMachines.init();
        GTFoods.init();
        GTItems.init();
        GregTechDatagen.init();

        // fabric exclusive, squeeze this in here to register before stuff is used
        GTRegistries.REGISTRATE.registerRegistrate();
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistryObjectBuilderTypes.registerFor(GTRegistryObjectBuilderTypes.WORLD_GEN_LAYER.registryKey);
        }
        GTOres.init();
        GTFeatures.init();
    }
}
