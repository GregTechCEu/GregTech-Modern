package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.common.data.GTDataComponents;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

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
