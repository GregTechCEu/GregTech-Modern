package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.common.block.LampBlock;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (returnValue != null) {
            LampData data = handItem.getOrDefault(GTDataComponents.LAMP_DATA, LampData.EMPTY);
            returnValue = returnValue
                    .setValue(LampBlock.INVERTED, data.inverted())
                    .setValue(LampBlock.BLOOM, data.bloom())
                    .setValue(LampBlock.LIGHT, data.lit());
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
        LampData data = stack.getOrDefault(GTDataComponents.LAMP_DATA, LampData.EMPTY);
        state = state.setValue(LampBlock.INVERTED, data.inverted())
                .setValue(LampBlock.BLOOM, data.bloom())
                .setValue(LampBlock.LIGHT, data.lit());
        return getBlock().getRenderer(state);
    }

    public record LampData(boolean inverted, boolean bloom, boolean lit) {

        public static final LampData EMPTY = new LampData(false, false, false);
        public static final Codec<LampData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("inverted").forGetter(LampData::inverted),
                Codec.BOOL.fieldOf("bloom").forGetter(LampData::bloom),
                Codec.BOOL.fieldOf("lit").forGetter(LampData::lit)).apply(instance, LampData::new));
    }
}
