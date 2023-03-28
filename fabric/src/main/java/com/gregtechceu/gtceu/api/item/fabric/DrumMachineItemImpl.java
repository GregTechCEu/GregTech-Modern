package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.misc.fabric.FluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.fabric.SimpleThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.fabric.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.item.Item;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote DrumMachineItemImpl
 */
public class DrumMachineItemImpl extends DrumMachineItem {
    protected DrumMachineItemImpl(MetaMachineBlock block, Properties properties) {
        super(block, properties);
        FluidStorage.ITEM.registerForItems((itemStack, context) -> new FluidHandlerItemStack(context, GTMachines.DRUM_CAPACITY.get(getDefinition())), this);

    }

    public static DrumMachineItem create(MetaMachineBlock block, Item.Properties properties) {
        return new DrumMachineItemImpl(block, properties);
    }
}
