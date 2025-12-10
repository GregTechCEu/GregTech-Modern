package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.IActivableBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.LaserPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeType;
import com.gregtechceu.gtceu.common.pipelike.laser.LevelLaserPipeNet;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LaserPipeBlock extends PipeBlock<LaserPipeType, LaserPipeProperties, LevelLaserPipeNet> implements IActivableBlock {

    private final LaserPipeProperties properties;

    public LaserPipeBlock(Properties properties, LaserPipeType type) {
        super(properties, type);
        this.properties = LaserPipeProperties.INSTANCE;
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (state, level, pos, index) -> {
            if (pos != null && level != null &&
                    level.getBlockEntity(pos) instanceof PipeBlockEntity<?, ?> pipe) {
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
    public LevelLaserPipeNet getWorldPipeNet(ServerLevel world) {
        return LevelLaserPipeNet.getOrCreate(world);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<LaserPipeType, LaserPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.LASER_PIPE.get();
    }

    @Override
    public LaserPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return LaserPipeProperties.INSTANCE;
    }

    @Override
    public LaserPipeProperties createProperties(IPipeNode<LaserPipeType, LaserPipeProperties> pipeTile) {
        LaserPipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) return getFallbackType();
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public LaserPipeProperties getFallbackType() {
        return LaserPipeProperties.INSTANCE;
    }

    @Override
    public PipeModel createPipeModel() {
        return PipeModel.create(this, LaserPipeType.NORMAL.getThickness(),
                GTCEu.id("block/pipe/pipe_laser_side"), GTCEu.id("block/pipe/pipe_laser_in"));
    }

    @Override
    public boolean canPipesConnect(IPipeNode<LaserPipeType, LaserPipeProperties> selfTile, Direction side,
                                   IPipeNode<LaserPipeType, LaserPipeProperties> sideTile) {
        return selfTile instanceof LaserPipeBlockEntity && sideTile instanceof LaserPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<LaserPipeType, LaserPipeProperties> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null && tile.getCapability(GTCapability.CAPABILITY_LASER, side.getOpposite()).isPresent();
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }

    @Override
    public boolean isActive(BlockState state, @Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof LaserPipeBlockEntity laserPipe)) return false;
        return laserPipe.isActive();
    }

    @Override
    public BlockState setActive(boolean value, BlockState currentState, @Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof LaserPipeBlockEntity laserPipe)) return currentState;
        laserPipe.setActive(value, 100);
        return currentState;
    }
}
