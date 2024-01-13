package com.gregtechceu.gtceu.integration.top;

import com.gregtechceu.gtceu.integration.top.provider.*;
import mcjty.theoneprobe.api.ITheOneProbe;

public class TheOneProbePlugin {

    public static void init(ITheOneProbe oneProbe) {
        oneProbe.registerProvider(new ElectricContainerInfoProvider());
//        oneProbe.registerProvider(new FuelableInfoProvider());
        oneProbe.registerProvider(new WorkableInfoProvider());
        oneProbe.registerProvider(new ControllableInfoProvider());
//        oneProbe.registerProvider(new DebugPipeNetInfoProvider());
//        oneProbe.registerProvider(new TransformerInfoProvider());
//        oneProbe.registerProvider(new DiodeInfoProvider());
//        oneProbe.registerProvider(new MultiblockInfoProvider());
//        oneProbe.registerProvider(new MaintenanceInfoProvider());
//        oneProbe.registerProvider(new MultiRecipeMapInfoProvider());
//        oneProbe.registerProvider(new ConverterInfoProvider());
        oneProbe.registerProvider(new RecipeLogicInfoProvider());
//        oneProbe.registerProvider(new PrimitivePumpInfoProvider());
        oneProbe.registerProvider(new CoverProvider());
    }

}
