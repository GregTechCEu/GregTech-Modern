package com.gregtechceu.gtceu.api.fluids.forge;

import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.extensions.IForgeFluid;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class GTFluidImpl extends GTFluid implements IForgeFluid {
    private final Supplier<FluidType> fluidType;

    public GTFluidImpl(@NotNull ResourceLocation fluidName, @NotNull FluidState state, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime, Supplier<FluidType> fluidType) {
        super(fluidName, state, flowingFluid, block, bucket, burnTime);
        this.fluidType = fluidType;
    }

    @Override
    public FluidType getFluidType() {
        return fluidType.get();
    }
}
