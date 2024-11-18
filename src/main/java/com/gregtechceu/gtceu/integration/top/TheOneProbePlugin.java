package com.gregtechceu.gtceu.integration.top;

import com.gregtechceu.gtceu.integration.top.provider.*;

import mcjty.theoneprobe.api.ITheOneProbe;

public class TheOneProbePlugin {

    public static void init(ITheOneProbe oneProbe) {
        oneProbe.registerProvider(new ElectricContainerInfoProvider());
        // oneProbe.registerProvider(new FuelableInfoProvider());
        oneProbe.registerProvider(new WorkableInfoProvider());
        oneProbe.registerProvider(new ControllableInfoProvider());
        // oneProbe.registerProvider(new DebugPipeNetInfoProvider());
        // oneProbe.registerProvider(new DiodeInfoProvider());
        // oneProbe.registerProvider(new MultiblockInfoProvider());
        // oneProbe.registerProvider(new MultiRecipeMapInfoProvider());
        // oneProbe.registerProvider(new ConverterInfoProvider());
        oneProbe.registerProvider(new RecipeLogicInfoProvider());
        oneProbe.registerProvider(new ParallelProvider());
        oneProbe.registerProvider(new RecipeOutputProvider());
        oneProbe.registerProvider(new MulitblockStructureProvider());
        oneProbe.registerProvider(new MaintenanceInfoProvider());
        oneProbe.registerProvider(new ExhaustVentInfoProvider());
        oneProbe.registerProvider(new SteamBoilerInfoProvider());
        oneProbe.registerProvider(new AutoOutputInfoProvider());
        oneProbe.registerProvider(new CableInfoProvider());
        oneProbe.registerProvider(new MachineModeProvider());
        oneProbe.registerProvider(new StainedColorProvider());
        oneProbe.registerProvider(new PrimitivePumpProvider());
        oneProbe.registerProvider(new CoverProvider());
        oneProbe.registerProvider(new HazardCleanerInfoProvider());
        oneProbe.registerProvider(new TransformerInfoProvider());
    }
}
