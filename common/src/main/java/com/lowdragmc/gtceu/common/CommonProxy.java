package com.lowdragmc.gtceu.common;

import com.lowdragmc.gtceu.api.gui.CoverUIFactory;
import com.lowdragmc.gtceu.api.gui.MachineUIFactory;
import com.lowdragmc.gtceu.common.libs.*;
import com.lowdragmc.gtceu.common.libs.materials.GTFoods;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote CommonProxy
 */
public class CommonProxy {

    public static void init() {
        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
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
    }

}
