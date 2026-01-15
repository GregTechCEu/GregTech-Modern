package com.gregtechceu.gtceu.integration.top;

import com.gregtechceu.gtceu.integration.top.element.FluidStackElement;
import com.gregtechceu.gtceu.integration.top.element.ProgressElement;
import com.gregtechceu.gtceu.integration.top.provider.*;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mcjty.theoneprobe.api.ITheOneProbe;

public class TheOneProbePlugin {

    // whose idea was it to make the expected object a Function<ITheOneProbe, Void> and not a Consumer<ITheOneProbe>???
    public static Void init(ITheOneProbe probe) {
        probe.registerElementFactory(new IElementFactory() {

            @Override
            public IElement createElement(RegistryFriendlyByteBuf friendlyByteBuf) {
                return new FluidStackElement(friendlyByteBuf);
            }

            @Override
            public ResourceLocation getId() {
                return FluidStackElement.ID;
            }
        });
        probe.registerElementFactory(new ProgressElement.Factory());

        probe.registerProvider(new ElectricContainerInfoProvider());
        // probe.registerProvider(new FuelableInfoProvider());
        probe.registerProvider(new WorkableInfoProvider());
        probe.registerProvider(new ControllableInfoProvider());
        // probe.registerProvider(new DebugPipeNetInfoProvider());
        // probe.registerProvider(new DiodeInfoProvider());
        // probe.registerProvider(new MultiblockInfoProvider());
        // probe.registerProvider(new MultiRecipeMapInfoProvider());
        // probe.registerProvider(new ConverterInfoProvider());
        probe.registerProvider(new RecipeLogicInfoProvider());
        probe.registerProvider(new ParallelProvider());
        probe.registerProvider(new RecipeOutputProvider());
        probe.registerProvider(new MultiblockStructureProvider());
        probe.registerProvider(new MaintenanceInfoProvider());
        probe.registerProvider(new ExhaustVentInfoProvider());
        probe.registerProvider(new SteamBoilerInfoProvider());
        probe.registerProvider(new AutoOutputInfoProvider());
        probe.registerProvider(new CableInfoProvider());
        probe.registerProvider(new MachineModeProvider());
        probe.registerProvider(new StainedColorProvider());
        probe.registerProvider(new PrimitivePumpProvider());
        probe.registerProvider(new DataBankInfoProvider());
        probe.registerProvider(new CoverProvider());
        probe.registerProvider(new HazardCleanerInfoProvider());
        probe.registerProvider(new TransformerInfoProvider());
        probe.registerProvider(new EnergyConverterModeProvider());
        return null;
    }
}
