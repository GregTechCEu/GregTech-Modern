package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

public class PipeBlockItem extends BlockItem {

    public PipeBlockItem(PipeBlock block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull PipeBlock getBlock() {
        return (PipeBlock) super.getBlock();
    }

    @Override
    public boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        var clickedPos = pos.relative(side.getOpposite());
        var baseNode = getBlock().getBlockEntity(level, clickedPos);
        if (baseNode != null) {
            var sideAttach = ICoverable
                    .determineGridSideHit(new BlockHitResult(context.getClickLocation(), side, clickedPos, false));
            if (sideAttach != null && level.isEmptyBlock(clickedPos.relative(sideAttach))) {
                pos = clickedPos.relative(sideAttach);
                side = sideAttach;
                context = BlockPlaceContext.at(context, clickedPos, sideAttach);
            }
        }

        if (super.placeBlock(context, state)) {
            ItemStack offhand = context.getPlayer().getOffhandItem();
            for (int i = 0; i < DyeColor.values().length; i++) {
                if (offhand.is(GTItems.SPRAY_CAN_DYES[i].get())) {
                    ((IInteractionItem) GTItems.SPRAY_CAN_DYES[i].get().getComponents().get(0)).use(
                            GTItems.SPRAY_CAN_DYES[i].get(), level,
                            context.getPlayer(), InteractionHand.OFF_HAND);
                    break;
                }
            }

            PipeBlockEntity tile = getBlock().getBlockEntity(level, pos);
            if (tile != null) {
                tile.placedBy(context.getItemInHand(), context.getPlayer());
                getBlock().doPlacementLogic(tile, side.getOpposite());
            }
            return true;
        } else return false;
    }


}
