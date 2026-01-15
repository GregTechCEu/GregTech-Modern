package com.gregtechceu.gtceu.integration.cctweaked;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.placeholder.*;
import com.gregtechceu.gtceu.api.placeholder.exceptions.NotSupportedException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.*;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.ForgeComputerCraftAPI;

import java.util.List;

public class CCTweakedPlugin {

    public static void init() {
        ComputerCraftAPI.registerGenericSource(new ControllablePeripheral());
        ComputerCraftAPI.registerGenericSource(new EnergyInfoPeripheral());
        ComputerCraftAPI.registerGenericSource(new TurbineMachinePeripheral());
        ComputerCraftAPI.registerGenericSource(new WorkablePeripheral());
        ComputerCraftAPI.registerGenericSource(new CoverHolderPeripheral());
        ComputerCraftAPI.registerGenericSource(new CentralMonitorPeripheral());
        ForgeComputerCraftAPI.registerGenericCapability(GTCapability.CAPABILITY_CONTROLLABLE);
        ForgeComputerCraftAPI.registerGenericCapability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER);
        ForgeComputerCraftAPI.registerGenericCapability(GTCapability.CAPABILITY_TURBINE_MACHINE);
        ForgeComputerCraftAPI.registerGenericCapability(GTCapability.CAPABILITY_WORKABLE);
        ForgeComputerCraftAPI.registerGenericCapability(GTCapability.CAPABILITY_COVERABLE);
        ForgeComputerCraftAPI.registerGenericCapability(GTCapability.CAPABILITY_CENTRAL_MONITOR);
        PlaceholderHandler.addPlaceholder(new Placeholder("bufferText") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                if (!(ctx.cover() instanceof IPlaceholderInfoProviderCover cover)) throw new NotSupportedException();
                int i = PlaceholderUtils.toInt(args.get(0));
                PlaceholderUtils.checkRange("line number", 1, 100, i);
                return MultiLineComponent.of(cover.getComputerCraftTextBuffer().get(i - 1));
            }
        });
    }
}
