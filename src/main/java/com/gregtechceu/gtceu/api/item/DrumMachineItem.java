package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote DrumMachineItem
 */
public class DrumMachineItem extends MetaMachineItem {

    private Material mat;

    protected DrumMachineItem(IMachineBlock block, Properties properties, Material mat) {
        super(block, properties);
        this.mat = mat;
    }

    public static DrumMachineItem create(IMachineBlock block, Properties properties, Material mat) {
        return new DrumMachineItem(block, properties, mat);
    }

    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        FluidPipeProperties property;
        if (mat.hasProperty(PropertyKey.FLUID_PIPE))
            property = mat.getProperty(PropertyKey.FLUID_PIPE);
        else property = null;

        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM && property != null) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(
                    () -> new ThermalFluidHandlerItemStack(
                            itemStack,
                            Math.toIntExact(GTMachineUtils.DRUM_CAPACITY.get(getDefinition())),
                            property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(),
                            property.isCryoProof(), property.isPlasmaProof())));
        }
        return LazyOptional.empty();
    }
}
