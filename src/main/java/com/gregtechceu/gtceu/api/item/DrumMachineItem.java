package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

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

    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(
                    () -> new FluidHandlerItemStack(
                            itemStack,
                            Math.toIntExact(GTMachines.DRUM_CAPACITY.get(getDefinition())))));
        }
        return LazyOptional.empty();
    }
}
