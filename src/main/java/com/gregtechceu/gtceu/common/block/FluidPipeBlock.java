package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.LevelFluidPipeNet;
import com.gregtechceu.gtceu.utils.EntityDamageUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.List;

public class FluidPipeBlock extends MaterialPipeBlock<FluidPipeType, FluidPipeProperties, LevelFluidPipeNet> {

    public FluidPipeBlock(Properties properties, FluidPipeType fluidPipeType, Material material) {
        super(properties, fluidPipeType, material);
    }

    @Override
    protected FluidPipeProperties createProperties(FluidPipeType fluidPipeType, Material material) {
        return fluidPipeType.modifyProperties(material.getProperty(PropertyKey.FLUID_PIPE));
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof FluidPipeBlockEntity fluidPipeBlockEntity) {
                if (side != null && fluidPipeBlockEntity.isConnected(side)) {
                    return fluidPipeBlockEntity.getTankList(side);
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COVERABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof PipeBlockEntity<?, ?> pipe) {
                return pipe.getCoverContainer();
            }
            return null;
        }, this);
    }

    @Override
    protected FluidPipeProperties createMaterialData() {
        return material.getProperty(PropertyKey.FLUID_PIPE);
    }

    @Override
    public LevelFluidPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelFluidPipeNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<FluidPipeType, FluidPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.FLUID_PIPE.get();
    }

    @Override
    public boolean canPipesConnect(IPipeNode<FluidPipeType, FluidPipeProperties> selfTile, Direction side,
                                   IPipeNode<FluidPipeType, FluidPipeProperties> sideTile) {
        return selfTile instanceof FluidPipeBlockEntity && sideTile instanceof FluidPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<FluidPipeType, FluidPipeProperties> selfTile,
                                         Direction side, Level level, BlockPos pos) {
        return FluidUtil.getFluidHandler(level, pos, side.getOpposite()).isPresent();
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeType.createPipeModel(this, material, provider);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
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
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // dont apply damage if there is a frame box
        var pipeNode = getPipeTile(level, pos);
        if (pipeNode == null) {
            GTCEu.LOGGER.error("Pipe was null");
            return;
        }
        if (!pipeNode.getFrameMaterial().isNull()) {
            BlockState frameState = GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipeNode.getFrameMaterial())
                    .getDefaultState();
            ((MaterialBlock) frameState.getBlock()).entityInside(frameState, level, pos, entity);
            return;
        }
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) == null) return;
        FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) level.getBlockEntity(pos);

        if (pipe.getOffsetTimer() % 10 == 0) {
            if (entity instanceof LivingEntity livingEntity) {
                if (pipe.getFluidTanks().length > 1) {
                    // apply temperature damage for the hottest and coldest pipe (multi fluid pipes)
                    int maxTemperature = Integer.MIN_VALUE;
                    int minTemperature = Integer.MAX_VALUE;
                    for (var tank : pipe.getFluidTanks()) {
                        FluidStack stack = tank.getFluid();
                        if (!tank.getFluid().isEmpty()) {
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
                    if (!tank.getFluid().isEmpty()) {
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
