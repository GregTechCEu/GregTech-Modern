package com.gregtechceu.gtceu.api.items;


import com.gregtechceu.gtceu.api.blocks.IMachineBlock;
import com.gregtechceu.gtceu.data.GTDataComponents;
import com.gregtechceu.gtceu.data.GTMachines;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote DrumMachineItem
 */
public class DrumMachineItem extends MetaMachineItem {
    protected DrumMachineItem(IMachineBlock block, Properties properties) {
        super(block, properties);
    }

    public static DrumMachineItem create(IMachineBlock block, Properties properties) {
        return new DrumMachineItem(block, properties);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ignored) -> new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack, GTMachines.DRUM_CAPACITY.getInt(getDefinition())), this);
    }
}
