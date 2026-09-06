package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DrumMachineItem extends MetaMachineItem {

    private final Material mat;

    protected DrumMachineItem(MetaMachineBlock block, Properties properties, Material mat) {
        super(block, properties);
        this.mat = Objects.requireNonNull(mat, "Drum Material cannot be null");
    }

    public static DrumMachineItem create(MetaMachineBlock block, Properties properties, Material mat) {
        return new DrumMachineItem(block, properties, mat);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        final FluidPipeProperties property = mat.getProperty(PropertyKey.FLUID_PIPE);
        if (property == null) {
            return null;
        }
        return new ThermalFluidHandlerItemStack(stack,
                GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition()),
                property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(),
                property.isCryoProof(), property.isPlasmaProof());
    }
}
