package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.data.GTItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
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
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (super.placeBlock(context, state)) {
            ItemStack offhand = context.getPlayer().getOffhandItem();
            for (int i = 0; i < DyeColor.values().length; i++) {
                if (offhand.is(GTItems.SPRAY_CAN_DYES[i].get())) {
                    ((IInteractionItem) GTItems.SPRAY_CAN_DYES[i].get().getComponents().get(0)).use(
                            GTItems.SPRAY_CAN_DYES[i].get(), context.getLevel(),
                            context.getPlayer(), InteractionHand.OFF_HAND);
                    break;
                }
            }

            PipeBlockEntity tile = getBlock().getBlockEntity(context.getLevel(), context.getClickedPos());
            if (tile != null) {
                tile.placedBy(context.getItemInHand(), context.getPlayer());
                getBlock().doPlacementLogic(tile, context.getClickedFace().getOpposite());
            }
            return true;
        } else return false;
    }
}
