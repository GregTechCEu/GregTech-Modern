package com.gregtechceu.gtceu.api.fluids;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class GTFluidType extends FluidType {

    private final Material material;

    public GTFluidType(Properties properties, Material material) {
        super(properties);
        this.material = material;
    }

    @Override
    public String getDescriptionId() {
        return material.getUnlocalizedName();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(super.getDescriptionId(), material.getLocalizedName());
    }

    @Override
    public Component getDescription(FluidStack stack) {
        return Component.translatable(super.getDescriptionId(stack), material.getLocalizedName());
    }

    @Override
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        if (!ConfigHolder.INSTANCE.gameplay.gasesVaporizeOnPlacement) {
            return false;
        }

        FluidStorage fluidStorage = material.getProperty(PropertyKey.FLUID);
        // always vaporize plasmas and gases
        Fluid plasma = fluidStorage.get(FluidStorageKeys.PLASMA);
        if (plasma != null) {
            return !plasma.defaultFluidState().createLegacyBlock().isEmpty();
        }
        Fluid gas = fluidStorage.get(FluidStorageKeys.GAS);
        if (gas != null) {
            return !gas.defaultFluidState().createLegacyBlock().isEmpty();
        }

        return false;
    }
}
