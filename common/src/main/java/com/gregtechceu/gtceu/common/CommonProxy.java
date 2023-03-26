package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.CoverUIFactory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.api.gui.MachineUIFactory;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.data.data.GregTechDatagen;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote CommonProxy
 */
public class CommonProxy {

    public static void init() {
        if (!GTCEu.isKubeJSLoaded()) {
            initInternal();
        }
    }

    /**
     * If kjs is loaded, make sure our mod is loaded after it. {@link com.gregtechceu.gtceu.core.mixins.kjs.KubeJSMixin}
     */
    public static void initInternal() {
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
