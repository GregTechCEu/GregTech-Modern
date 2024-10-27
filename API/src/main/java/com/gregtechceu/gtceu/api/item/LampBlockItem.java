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

import static com.gregtechceu.gtceu.common.block.LampBlock.isBloomEnabled;
import static com.gregtechceu.gtceu.common.block.LampBlock.isInverted;
import static com.gregtechceu.gtceu.common.block.LampBlock.isLightEnabled;

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

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState returnValue = super.getPlacementState(context);
        ItemStack handItem = context.getItemInHand();
        if (returnValue != null && handItem.hasTag()) {
            var tag = handItem.getTag();
            returnValue = returnValue
                    .setValue(LampBlock.INVERTED, isInverted(tag))
                    .setValue(LampBlock.BLOOM, isBloomEnabled(tag))
                    .setValue(LampBlock.LIGHT, isLightEnabled(tag));
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
            var tag = stack.getTag();
            state = state
                    .setValue(LampBlock.INVERTED, isInverted(tag))
                    .setValue(LampBlock.BLOOM, isBloomEnabled(tag))
                    .setValue(LampBlock.LIGHT, isLightEnabled(tag));
        }
        return getBlock().getRenderer(state);
    }
}
