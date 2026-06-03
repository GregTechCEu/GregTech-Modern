package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.utils.EntityDamageUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidPipeBlock extends MaterialPipeBlock<FluidPipeType, FluidPipeProperties> {

    public FluidPipeBlock(Properties properties, FluidPipeType fluidPipeType, Material material) {
        super(properties, fluidPipeType, material);
    }

    @Override
    protected FluidPipeProperties createProperties(FluidPipeType fluidPipeType, Material material) {
        return fluidPipeType.modifyProperties(material.getProperty(PropertyKey.FLUID_PIPE));
    }

    @Override
    protected FluidPipeProperties createMaterialData() {
        return material.getProperty(PropertyKey.FLUID_PIPE);
    }

    @Override
    public LevelPipeNet<FluidPipeProperties, FluidPipeNet> getWorldPipeNet(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelPipeNet<>(serverLevel, FluidPipeNet::new),
                () -> new LevelPipeNet<>(serverLevel, FluidPipeNet::new), "gtcue_fluid_pipe_net");
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<FluidPipeType, FluidPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.FLUID_PIPE.get();
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeType.createPipeModel(this, material, provider);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FluidPipeProperties properties = createProperties(defaultBlockState(), stack);

        tooltip.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", properties.getThroughput()));
        tooltip.add(Component.translatable("gtceu.fluid_pipe.max_temperature",
                FormattingUtil.formatTemperature(properties.getMaxFluidTemperature())));

        if (properties.getChannels() > 1) {
            tooltip.add(Component.translatable("gtceu.fluid_pipe.channels", properties.getChannels()));
        }

        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("gtceu.tooltip.fluid_pipe_hold_shift"));
            return;
        }

        if (properties.isGasProof())
            tooltip.add(Component.translatable("gtceu.fluid_pipe.gas_proof"));
        else
            tooltip.add(Component.translatable("gtceu.fluid_pipe.not_gas_proof"));

        if (properties.isAcidProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.acid_proof"));
        if (properties.isCryoProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.cryo_proof"));
        if (properties.isPlasmaProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.plasma_proof"));
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
