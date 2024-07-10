package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.common.block.LampBlock;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.block.LampBlock.TAG_BLOOM;
import static com.gregtechceu.gtceu.common.block.LampBlock.TAG_INVERTED;
import static com.gregtechceu.gtceu.common.block.LampBlock.TAG_LIT;

@ParametersAreNonnullByDefault
public class LampBlockItem extends BlockItem implements IItemRendererProvider {

    public LampBlockItem(LampBlock block, Properties properties) {
        super(block, properties);
    }

    @NotNull
    @Override
    public LampBlock getBlock() {
        return (LampBlock) super.getBlock();
    }

    public boolean isInverted(ItemStack stack) {
        return stack.getTag().getBoolean(TAG_INVERTED);
    }

    public boolean isLightEnabled(ItemStack stack) {
        return stack.getTag().getBoolean(TAG_LIT);
    }

    public boolean isBloomEnabled(ItemStack stack) {
        return stack.getTag().getBoolean(TAG_BLOOM);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState returnValue = super.getPlacementState(context);
        ItemStack handItem = context.getItemInHand();
        if (returnValue != null && handItem.hasTag()) {
            returnValue = returnValue
                    .setValue(LampBlock.INVERTED, handItem.getTag().getBoolean(TAG_INVERTED))
                    .setValue(LampBlock.BLOOM, handItem.getTag().getBoolean(TAG_BLOOM))
                    .setValue(LampBlock.LIGHT, handItem.getTag().getBoolean(TAG_LIT));
        }
        return returnValue;
    }

    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        for (int i = 0; i < 8; ++i) {
            items.add(this.getBlock().getStackFromIndex(i));
        }
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        BlockState state = getBlock().defaultBlockState();
        if (stack.hasTag()) {
            state = state
                    .setValue(LampBlock.INVERTED, stack.getTag().getBoolean(TAG_INVERTED))
                    .setValue(LampBlock.BLOOM, stack.getTag().getBoolean(TAG_BLOOM))
                    .setValue(LampBlock.LIGHT, stack.getTag().getBoolean(TAG_LIT));
        }
        return getBlock().getRenderer(state);
    }
}
