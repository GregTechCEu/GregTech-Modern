package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.common.block.LampBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LampBlockItem extends BlockItem {

    public LampBlockItem(Block block, Properties properties) {
        super(block, properties);
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
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(LampBlock.TAG_INVERTED, (i & LampBlock.INVERTED_FLAG) == 0);
            tag.putBoolean(LampBlock.TAG_BLOOM, (i & LampBlock.BLOOM_FLAG) == 0);
            tag.putBoolean(LampBlock.TAG_LIT, (i & LampBlock.LIGHT_FLAG) == 0);
            ItemStack stack = new ItemStack(this);
            stack.setTag(tag);
            items.add(stack);
        }
    }
}
