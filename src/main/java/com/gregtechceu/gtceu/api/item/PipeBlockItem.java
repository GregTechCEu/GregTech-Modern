package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PipeBlockItem extends BlockItem {

    public PipeBlockItem(PipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public PipeBlock<?> getBlock() {
        return (PipeBlock<?>) super.getBlock();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltip, isAdvanced);
        if (GTUtil.isShiftDown()) {
            var tool = getBlock().getPipeTuneTool();
            tooltip.add(Component.translatable("gtceu.tool_action." + tool.name + ".connect"));
        } else {
            tooltip.add(Component.translatable("gtceu.tool_action.show_tooltips"));
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        var realPos = pos.relative(side.getOpposite());
        var baseNode = PipeBlock.getPipeBE(level, realPos);

        if (baseNode != null) {
            var sideAttach = ICoverable
                    .traceCoverSide(new BlockHitResult(context.getClickLocation(), side, realPos, false));
            if (sideAttach != null && context.getLevel().isEmptyBlock(realPos.relative(sideAttach))) {
                pos = realPos.relative(sideAttach);
                side = sideAttach;
                context = new BlockPlaceContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
                        new BlockHitResult(context.getClickLocation(), sideAttach, realPos, false));
            }
        }

        boolean didPlace = super.placeBlock(context, state);

        if (didPlace && !level.isClientSide) {

            PipeBlockEntity selfTile = PipeBlock.getPipeBE(level, pos);
            if (selfTile == null) return true;

            selfTile.tryConnectToAdjacent(side.getOpposite(), false);

            // for (Direction facing : GTUtil.DIRECTIONS) {
            // var adjacentBE = selfTile.getNeighbor(facing);
            //
            // if (adjacentBE instanceof PipeBlockEntity otherPipe) {
            // if (otherPipe.isConnected(facing.getOpposite())) {
            // if (otherPipe.canConnect(facing.getOpposite())) {
            // selfTile.setConnection(facing, true, true);
            // } else {
            // otherPipe.setConnection(facing.getOpposite(), false, true);
            // }
            // }
            // } else if (!ConfigHolder.INSTANCE.machines.gt6StylePipesCables) {
            // selfTile.tryConnectToAdjacent(facing, false);
            // }
            //
            // }
        }
        return didPlace;
    }
}
