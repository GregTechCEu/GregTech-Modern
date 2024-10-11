package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.data.machine.GTMachines;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;

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

    private Material mat;

    protected DrumMachineItem(IMachineBlock block, Properties properties, Material mat) {
        super(block, properties);
        this.mat = mat;
    }

    public static DrumMachineItem create(IMachineBlock block, Properties properties, Material mat) {
        return new DrumMachineItem(block, properties, mat);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if(mat.hasProperty(PropertyKey.FLUID_PIPE)) {
            FluidPipeProperties property = mat.getProperty(PropertyKey.FLUID_PIPE);
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, ignored) -> new ThermalFluidHandlerItemStack(stack, GTMachines.DRUM_CAPACITY.getInt(getDefinition()),
                            property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(), property.isCryoProof(),
                            property.isPlasmaProof()), this);
        } else {
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, ignored) -> new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack,
                            GTMachines.DRUM_CAPACITY.getInt(getDefinition())),
                    this);
        }
    }
}

/*
package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.GTMachines;

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
/*
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
                            Math.toIntExact(GTMachines.DRUM_CAPACITY.get(getDefinition())),
                            property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(),
                            property.isCryoProof(), property.isPlasmaProof())));
        }
        return LazyOptional.empty();
    }
}

*/
