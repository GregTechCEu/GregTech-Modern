package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.utils.EntityDamageUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidPipeBlock extends MaterialPipeBlock {

    public FluidPipeBlock(Properties properties, FluidPipeVariant fluidPipeType, Material material) {
        super(properties, fluidPipeType, GTPipeNetworks.FLUID, material);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // dont apply damage if there is a frame box
        FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) level.getBlockEntity(pos);

        if (pipe == null) {
            GTCEu.LOGGER.error("Pipe was null");
            return;
        }
        if (!pipe.getFrameMaterial().isNull()) {
            BlockState frameState = GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipe.getFrameMaterial())
                    .getDefaultState();
            frameState.getBlock().entityInside(frameState, level, pos, entity);
            return;
        }
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) == null) return;

        if (pipe.getOffsetTimer() % 10 == 0) {
            if (entity instanceof LivingEntity livingEntity) {
                if (pipe.getFluidTanks().length > 1) {
                    // apply temperature damage for the hottest and coldest pipe (multi fluid pipes)
                    int maxTemperature = Integer.MIN_VALUE;
                    int minTemperature = Integer.MAX_VALUE;
                    for (var tank : pipe.getFluidTanks()) {
                        FluidStack stack = tank.getFluid();
                        if (tank.getFluid().getAmount() > 0) {
                            maxTemperature = Math.max(maxTemperature,
                                    stack.getFluid().getFluidType().getTemperature(stack));
                            minTemperature = Math.min(minTemperature,
                                    stack.getFluid().getFluidType().getTemperature(stack));
                        }
                    }
                    if (maxTemperature != Integer.MIN_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage(livingEntity, maxTemperature, 1.0F, 20);
                    }
                    if (minTemperature != Integer.MAX_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage(livingEntity, minTemperature, 1.0F, 20);
                    }
                } else {
                    var tank = pipe.getFluidTanks()[0];
                    if (tank.getFluid().getAmount() > 0) {
                        // Apply temperature damage for the pipe (single fluid pipes)
                        FluidStack stack = tank.getFluid();
                        EntityDamageUtil.applyTemperatureDamage(livingEntity,
                                stack.getFluid().getFluidType().getTemperature(stack), 1.0F, 20);
                    }
                }
            }
        }
        super.entityInside(state, level, pos, entity);
    }
}
