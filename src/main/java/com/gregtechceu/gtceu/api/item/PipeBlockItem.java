package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/6/23
 * @implNote PipeBlockItem
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PipeBlockItem extends BlockItem {

    public PipeBlockItem(PipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public PipeBlock getBlock() {
        return (PipeBlock) super.getBlock();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean superVal = super.placeBlock(context, state);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        if (superVal && !level.isClientSide) {
            IPipeNode selfTile = (IPipeNode) level.getBlockEntity(pos);
            if (selfTile == null) return superVal;
            if (selfTile.getPipeBlock().canConnect(selfTile, side.getOpposite())) {
                selfTile.setConnection(side.getOpposite(), true, false);
            }
            for (Direction facing : GTUtil.DIRECTIONS) {
                BlockEntity te = selfTile.getNeighbor(facing);
                if (te instanceof IPipeNode otherPipe) {
                    if (otherPipe.isConnected(facing.getOpposite())) {
                        if (otherPipe.getPipeBlock().canPipesConnect(otherPipe, facing.getOpposite(), selfTile)) {
                            selfTile.setConnection(facing, true, true);
                        } else {
                            otherPipe.setConnection(facing.getOpposite(), false, true);
                        }
                    }
                } else if (!ConfigHolder.INSTANCE.machines.gt6StylePipesCables && selfTile.getPipeBlock().canPipeConnectToBlock(selfTile, facing, te)) {
                    selfTile.setConnection(facing, true, false);
                }
            }
        }
        return superVal;
    }
}
