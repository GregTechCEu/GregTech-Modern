package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.VariantBlock;
import com.gregtechceu.gtceu.client.renderer.item.VariantBlockItemRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote VariantBlockItem
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariantBlockItem<R extends Enum<R> & StringRepresentable, T extends VariantBlock<R>> extends BlockItem implements IItemRendererProvider {
    public VariantBlockItem(T block, Properties properties) {
        super(block, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getBlock() {
        return (T) super.getBlock();
    }

    public BlockState getBlockState(ItemStack itemStack) {
        var variant = getVariant(itemStack);
        if (variant != null) {
            return getBlock().getState(variant);
        }
        return getBlock().defaultBlockState();
    }

    @Nullable
    public R getVariant(ItemStack itemStack) {
        if (itemStack.is(this)) {
            var tag = itemStack.getTag();
            if (tag != null && tag.contains("variant")) {
                var variant = tag.getString("variant");
                for (R value : getBlock().getVariantValues()) {
                    if (value.getSerializedName().equals(variant)) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    public void setVariant(ItemStack itemStack, R variant) {
        if (itemStack.is(this)) {
            itemStack.getOrCreateTag().putString("variant", variant.getSerializedName());
        }
    }

    @Override
    public String getDescriptionId(ItemStack itemStack) {
        var variant = getVariant(itemStack);
        if (variant == null) {
            return super.getDescriptionId();
        }
        return super.getDescriptionId() + '.' + variant.getSerializedName();
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        if (this.allowedIn(category)) {
            for (R variant : getBlock().getVariantValues()) {
                var item = getDefaultInstance();
                setVariant(item, variant);
                items.add(item);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        getBlock().appendHoverText(getVariant(stack), stack, level, tooltipComponents, isAdvanced);
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return VariantBlockItemRenderer.INSTANCE;
    }
}
