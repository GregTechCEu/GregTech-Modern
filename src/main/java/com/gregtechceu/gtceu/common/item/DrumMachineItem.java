package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import org.jetbrains.annotations.NotNull;

public class DrumMachineItem extends MetaMachineItem {

    @NotNull
    private final Material mat;

    protected DrumMachineItem(IMachineBlock block, Properties properties, @NotNull Material mat) {
        super(block, properties);
        this.mat = mat;
    }

    public static DrumMachineItem create(IMachineBlock block, Properties properties, @NotNull Material mat) {
        return new DrumMachineItem(block, properties, mat);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (mat.hasProperty(PropertyKey.FLUID_PIPE)) {
            FluidPipeProperties property = mat.getProperty(PropertyKey.FLUID_PIPE);
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, ignored) -> new ThermalFluidHandlerItemStack(stack,
                            GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition()),
                            property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(),
                            property.isCryoProof(),
                            property.isPlasmaProof()),
                    this);
        } else {
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, ignored) -> new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack,
                            GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition())),
                    this);
        }
    }
}
