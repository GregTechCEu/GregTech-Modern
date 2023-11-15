package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeNet;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeProperties;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeType;
import com.gregtechceu.gtceu.common.pipelike.laser.LevelLaserPipeNet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public class LaserPipeBlock extends PipeBlock<LaserPipeType, LaserPipeNet.LaserData, LevelLaserPipeNet> {

    public final PipeBlockRenderer renderer;
    public final PipeModel model;

    public LaserPipeBlock(Properties properties, LaserPipeType type) {
        super(properties, type);
        this.model = new PipeModel(LaserPipeType.NORMAL.getThickness(), () -> GTCEu.id("block/pipe/pipe_laser_side"), () -> GTCEu.id("block/pipe/pipe_laser_in"), null, null);
        this.renderer = new PipeBlockRenderer(this.model);
    }

    @Environment(EnvType.CLIENT)
    public static BlockColor tintedColor() {
        return (blockState, level, blockPos, index) -> {
            if (blockPos != null && level != null && level.getBlockEntity(blockPos) instanceof PipeBlockEntity<?,?> pipe && pipe.isPainted()) {
                return pipe.getRealColor();
            }
            return -1;
        };
    }

    @Override
    public LevelLaserPipeNet getWorldPipeNet(ServerLevel world) {
        return LevelLaserPipeNet.getOrCreate(world);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<LaserPipeType, LaserPipeNet.LaserData>> getBlockEntityType() {
        return GTBlockEntities.LASER_PIPE.get();
    }

    @Override
    public LaserPipeNet.LaserData createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return new LaserPipeNet.LaserData(BlockPos.ZERO, Direction.NORTH, 0, LaserPipeProperties.INSTANCE, (byte) 0);
    }

    @Override
    public LaserPipeNet.LaserData getFallbackType() {
        return new LaserPipeNet.LaserData(BlockPos.ZERO, Direction.NORTH, 0, LaserPipeProperties.INSTANCE, (byte) 0);
    }

    @Override
    public @Nullable PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Override
    protected PipeModel getPipeModel() {
        return model;
    }
}
