package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.ActivablePipeModel;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.optical.LevelOpticalPipeNet;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeType;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OpticalPipeBlock extends PipeBlock<OpticalPipeType, OpticalPipeProperties, LevelOpticalPipeNet> {

    private final OpticalPipeProperties properties;

    public OpticalPipeBlock(BlockBehaviour.Properties properties, OpticalPipeType pipeType) {
        super(properties, pipeType);
        this.properties = OpticalPipeProperties.INSTANCE;

        registerDefaultState(defaultBlockState().setValue(GTBlockStateProperties.ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(GTBlockStateProperties.ACTIVE);
    }

    @Override
    public @NotNull PipeModel createPipeModel(GTBlockstateProvider provider) {
        ActivablePipeModel pipeModel = new ActivablePipeModel(this, pipeType.getThickness(),
                GTCEu.id("block/pipe/pipe_optical_side"), GTCEu.id("block/pipe/pipe_optical_in"),
                provider);
        pipeModel.setSideOverlay(GTCEu.id("block/pipe/pipe_optical_side_overlay"));
        pipeModel.setSideOverlayActive(GTCEu.id("block/pipe/pipe_optical_side_overlay_active"));
        return pipeModel;
    }

    @Override
    public LevelOpticalPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelOpticalPipeNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<OpticalPipeType, OpticalPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.OPTICAL_PIPE.get();
    }

    @Override
    public OpticalPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return null;
    }

    @Override
    public OpticalPipeProperties createProperties(@NotNull IPipeNode<OpticalPipeType, OpticalPipeProperties> pipeTile) {
        OpticalPipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) return getFallbackType();
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public OpticalPipeProperties getFallbackType() {
        return OpticalPipeProperties.INSTANCE;
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (blockState, level, blockPos, index) -> {
            if (blockPos != null && level != null &&
                    level.getBlockEntity(blockPos) instanceof PipeBlockEntity<?, ?> pipe) {
                if (!pipe.getFrameMaterial().isNull()) {
                    if (index == 3) {
                        return pipe.getFrameMaterial().getMaterialRGB();
                    } else if (index == 4) {
                        return pipe.getFrameMaterial().getMaterialSecondaryRGB();
                    }
                }
                if (pipe.isPainted()) {
                    return pipe.getRealColor();
                }
            }
            return -1;
        };
    }

    @Override
    public boolean canPipesConnect(IPipeNode<OpticalPipeType, OpticalPipeProperties> selfTile, Direction side,
                                   IPipeNode<OpticalPipeType, OpticalPipeProperties> sideTile) {
        return selfTile instanceof OpticalPipeBlockEntity && sideTile instanceof OpticalPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<OpticalPipeType, OpticalPipeProperties> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        if (tile == null) return false;
        if (tile.getCapability(GTCapability.CAPABILITY_DATA_ACCESS, side.getOpposite()).isPresent()) return true;
        return tile.getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, side.getOpposite()).isPresent();
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }
}
