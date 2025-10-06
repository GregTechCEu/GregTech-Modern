package com.gregtechceu.gtceu.integration.top;

import com.gregtechceu.gtceu.integration.top.element.FluidStackElement;
import com.gregtechceu.gtceu.integration.top.element.FluidStyle;
import com.gregtechceu.gtceu.integration.top.element.ProgressElement;
import com.gregtechceu.gtceu.integration.top.provider.*;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mcjty.theoneprobe.api.ITheOneProbe;

public class TheOneProbePlugin {

    public static void init(ITheOneProbe oneProbe) {
        oneProbe.registerElementFactory(new IElementFactory() {

            private ResourceLocation id = null;

            @Override
            public IElement createElement(FriendlyByteBuf friendlyByteBuf) {
                return new FluidStackElement(friendlyByteBuf);
            }

            @Override
            public ResourceLocation getId() {
                if (id == null)
                    id = new FluidStackElement(FluidStack.EMPTY, new FluidStyle()).getID();
                return id;
            }
        });
        oneProbe.registerElementFactory(new ProgressElement.Factory());

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
        oneProbe.registerProvider(new MultiblockStructureProvider());
        oneProbe.registerProvider(new MaintenanceInfoProvider());
        oneProbe.registerProvider(new ExhaustVentInfoProvider());
        oneProbe.registerProvider(new SteamBoilerInfoProvider());
        oneProbe.registerProvider(new AutoOutputInfoProvider());
        oneProbe.registerProvider(new CableInfoProvider());
        oneProbe.registerProvider(new MachineModeProvider());
        oneProbe.registerProvider(new StainedColorProvider());
        oneProbe.registerProvider(new PrimitivePumpProvider());
        oneProbe.registerProvider(new DataBankInfoProvider());
        oneProbe.registerProvider(new CoverProvider());
        oneProbe.registerProvider(new HazardCleanerInfoProvider());
        oneProbe.registerProvider(new TransformerInfoProvider());
        oneProbe.registerProvider(new EnergyConverterModeProvider());
    }
}
