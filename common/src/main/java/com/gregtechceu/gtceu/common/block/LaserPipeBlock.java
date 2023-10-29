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
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public class LaserPipeBlock extends PipeBlock<LaserPipeType, LaserPipeNet.LaserData, LevelLaserPipeNet> {

    public final DyeColor color;
    public final PipeBlockRenderer renderer;
    public final PipeModel model;

    public LaserPipeBlock(BlockBehaviour.Properties properties, DyeColor color) {
        super(properties, LaserPipeType.NORMAL);
        this.color = color;
        this.model = new PipeModel(LaserPipeType.NORMAL.getThickness(), () -> GTCEu.id("block/pipe/pipe_laser_side"), () -> GTCEu.id("block/pipe/pipe_laser_in"));
        this.renderer = new PipeBlockRenderer(this.model);
    }

    @Environment(EnvType.CLIENT)
    public static BlockColor tintedColor() {
        return (blockState, level, blockPos, index) -> {
            if (blockState.getBlock() instanceof LaserPipeBlock block) {
                if (blockPos != null && level != null && level.getBlockEntity(blockPos) instanceof PipeBlockEntity<?,?> pipe && pipe.isPainted()) {
                    return pipe.getRealColor();
                }
                return block.tinted(blockState, level, blockPos, index);
            }
            return -1;
        };
    }

    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int index) {
        return color.getTextColor();
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
        return new LaserPipeNet.LaserData(pState.getValue(BlockStateProperties.FACING), new LaserPipeProperties());
    }

    @Override
    public LaserPipeNet.LaserData getFallbackType() {
        return new LaserPipeNet.LaserData(Direction.DOWN, new LaserPipeProperties());
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
