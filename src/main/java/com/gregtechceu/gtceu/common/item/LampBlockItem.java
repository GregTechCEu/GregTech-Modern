package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.client.renderer.block.LampItemRenderer;
import com.gregtechceu.gtceu.common.block.LampBlock;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.block.LampBlock.isBloomEnabled;
import static com.gregtechceu.gtceu.common.block.LampBlock.isInverted;
import static com.gregtechceu.gtceu.common.block.LampBlock.isLightEnabled;

@ParametersAreNonnullByDefault
public class LampBlockItem extends BlockItem {

    public LampBlockItem(LampBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public LampBlock getBlock() {
        return (LampBlock) super.getBlock();
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null) return null;

        return getStateFromStack(context.getItemInHand(), state);
    }

    public BlockState getStateFromStack(ItemStack stack, BlockState baseState) {
        if (!stack.hasTag() || !stack.is(this)) {
            return baseState;
        }
        var tag = stack.getTag();
        return baseState.setValue(LampBlock.INVERTED, isInverted(tag))
                .setValue(LampBlock.BLOOM, isBloomEnabled(tag))
                .setValue(LampBlock.LIGHT, isLightEnabled(tag));
    }

    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        for (int i = 0; i < 8; ++i) {
            items.add(this.getBlock().getStackFromIndex(i));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return LampItemRenderer.getInstance();
            }
        });
    }
}
