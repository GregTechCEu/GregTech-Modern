package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import gregtech.common.items.MetaItems;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.Player;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class ItemPipeBlock extends ItemBlock {

    public ItemPipeBlock(PipeBlock block) {
        super(block);
    }

    @Override
    public @NotNull PipeBlock getBlock() {
        return (PipeBlock) super.getBlock();
    }

    @Override
    public boolean placeBlockAt(@NotNull ItemStack stack, @NotNull Player player, @NotNull World world,
                                @NotNull BlockPos pos, @NotNull Direction side,
                                float hitX, float hitY, float hitZ, @NotNull IBlockState newState) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            ItemStack offhand = player.getHeldItemOffhand();
            for (int i = 0; i < EnumDyeColor.values().length; i++) {
                if (offhand.isItemEqual(MetaItems.SPRAY_CAN_DYES[i].getStackForm())) {
                    MetaItems.SPRAY_CAN_DYES[i].getBehaviours().get(0).onItemUse(player, world,
                            pos, EnumHand.OFF_HAND, Direction.UP, 0, 0, 0);
                    break;
                }
            }

            PipeBlockEntity tile = getBlock().getTileEntity(world, pos);
            if (tile != null) {
                tile.placedBy(stack, player);
                getBlock().doPlacementLogic(tile, side.getOpposite());
            }
            return true;
        } else return false;
    }
}
