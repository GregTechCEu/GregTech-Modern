package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.gui.compass.GTRecipeViewCreator;
import com.lowdragmc.lowdraglib.gui.compass.component.RecipeComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @author KilaBash
 * @date 2023/7/30
 * @implNote ClientProxy
 */
@Environment(EnvType.CLIENT)
public class ClientProxy {
    public static void init() {
        RecipeComponent.registerRecipeViewCreator(new GTRecipeViewCreator());
    }
}
