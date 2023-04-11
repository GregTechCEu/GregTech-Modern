package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.CoverUIFactory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.api.gui.MachineUIFactory;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote CommonProxy
 */
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
        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        GTPlacerTypes.init();
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        GTElements.init();
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
        GTRecipes.init();
        GregTechDatagen.init();

        // fabric exclusive, squeeze this in here to register before stuff is used
        GTRegistries.REGISTRATE.registerRegistrate();
        GTOres.init();
        GTFeatures.init();
    }

}
