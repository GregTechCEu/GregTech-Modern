package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.FluidConstants;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.LevelFluidPipeNet;
import com.gregtechceu.gtceu.utils.EntityDamageUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote FluidPipeBlock
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidPipeBlock extends MaterialPipeBlock<FluidPipeType, FluidPipeProperties, LevelFluidPipeNet> {

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
    public LevelFluidPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelFluidPipeNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<FluidPipeType, FluidPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.FLUID_PIPE.get();
    }

    @Override
    public boolean canPipesConnect(IPipeNode<FluidPipeType, FluidPipeProperties> selfTile, Direction side, IPipeNode<FluidPipeType, FluidPipeProperties> sideTile) {
        return selfTile instanceof FluidPipeBlockEntity && sideTile instanceof FluidPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<FluidPipeType, FluidPipeProperties> selfTile, Direction side, @Nullable BlockEntity tile) {
        return tile != null && tile.getCapability(ForgeCapabilities.FLUID_HANDLER, side.getOpposite()).isPresent();
    }

    @Override
    protected PipeModel createPipeModel() {
        return pipeType.createPipeModel(material);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FluidPipeProperties properties = createProperties(defaultBlockState(), stack);

        tooltip.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate", properties.getThroughput()));
        tooltip.add(Component.translatable("gtceu.fluid_pipe.max_temperature", properties.getMaxFluidTemperature()));

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
        if(level.isClientSide) return;
        FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) level.getBlockEntity(pos);
        if(pipe.getOffsetTimer() % 10 == 0) {
            if(entity instanceof LivingEntity livingEntity) {
                if(pipe.getFluidTanks().length > 1) {
                    // apply temperature damage for the hottest and coldest pipe (multi fluid pipes)
                    int maxTemperature = Integer.MIN_VALUE;
                    int minTemperature = Integer.MAX_VALUE;
                    for (var tank : pipe.getFluidTanks()) {
                        FluidStack stack = tank.getFluid();
                        net.minecraftforge.fluids.FluidStack forgeStack = FluidHelperImpl.toFluidStack(stack);
                        if (tank.getFluid() != null && tank.getFluid().getAmount() > 0) {
                            maxTemperature = Math.max(maxTemperature, stack.getFluid().getFluidType().getTemperature(forgeStack));
                            minTemperature = Math.min(minTemperature, stack.getFluid().getFluidType().getTemperature(forgeStack));
                        }
                    }
                    if (maxTemperature != Integer.MIN_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage(livingEntity, maxTemperature, 1.0F, 20);
                    }
                    if (minTemperature != Integer.MAX_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage(livingEntity, minTemperature, 1.0F, 20);
                    }
                }
                else {
                    var tank = pipe.getFluidTanks()[0];
                    if (tank.getFluid() != null && tank.getFluid().getAmount() > 0) {
                        // Apply temperature damage for the pipe (single fluid pipes)
                        FluidStack stack = tank.getFluid();
                        net.minecraftforge.fluids.FluidStack forgeStack = FluidHelperImpl.toFluidStack(stack);
                        EntityDamageUtil.applyTemperatureDamage(livingEntity, stack.getFluid().getFluidType().getTemperature(forgeStack), 1.0F, 20);
                    }
                }
            }
        }
    }
}
