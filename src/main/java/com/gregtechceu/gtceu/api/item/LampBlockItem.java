package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.common.block.LampBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LampBlockItem extends BlockItem {

    public LampBlockItem(LampBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public LampBlock getBlock() {
        return (LampBlock) super.getBlock();
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState returnValue = super.getPlacementState(context);
        ItemStack handItem = context.getItemInHand();
        if (handItem.hasTag()) {
            returnValue = returnValue
                    .setValue(LampBlock.INVERTED, handItem.getTag().getBoolean(LampBlock.TAG_INVERTED))
                    .setValue(LampBlock.BLOOM, handItem.getTag().getBoolean(LampBlock.TAG_BLOOM))
                    .setValue(LampBlock.LIGHT, handItem.getTag().getBoolean(LampBlock.TAG_LIT));
        }
        return returnValue;
    }

    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        for (int i = 0; i < 8; ++i) {
            items.add(this.getBlock().getStackFromIndex(i));
        }
    }
}
