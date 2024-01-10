package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeData;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.LevelFluidPipeNet;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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
public class FluidPipeBlock extends MaterialPipeBlock<FluidPipeType, FluidPipeData, LevelFluidPipeNet> {

    public FluidPipeBlock(Properties properties, FluidPipeType fluidPipeType, Material material) {
        super(properties, fluidPipeType, material);
    }

    @Override
    protected FluidPipeData createMaterialData() {
        return new FluidPipeData(material.getProperty(PropertyKey.FLUID_PIPE), (byte) 0);
    }

    @Override
    public LevelFluidPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelFluidPipeNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<FluidPipeType, FluidPipeData>> getBlockEntityType() {
        return GTBlockEntities.FLUID_PIPE.get();
    }

    @Override
    protected PipeModel createPipeModel() {
        return pipeType.createPipeModel(material);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        FluidPipeProperties properties = pipeType.modifyProperties(createRawData(defaultBlockState(), stack)).properties();

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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
