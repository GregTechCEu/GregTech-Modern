package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.compass.GTCompassUIConfig;
import com.gregtechceu.gtceu.api.gui.compass.GTRecipeViewCreator;
import com.gregtechceu.gtceu.api.gui.compass.MultiblockAction;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.compass.component.RecipeComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;

/**
 * @author KilaBash
 * @date 2023/7/30
 * @implNote ClientProxy
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    public ClientProxy(IEventBus modBus) {
        super(modBus);
        init();
    }

    public static void init() {
        RecipeComponent.registerRecipeViewCreator(new GTRecipeViewCreator());
        CompassManager.INSTANCE.registerUIConfig(GTCEu.MOD_ID, new GTCompassUIConfig());
        CompassManager.INSTANCE.registerAction("multiblock", MultiblockAction::new);
    }
}
