package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtlib.client.renderer.IItemRendererProvider;
import com.gregtechceu.gtlib.client.renderer.IRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @implNote MaterialBlockItem
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MaterialPipeBlockItem extends BlockItem implements IItemRendererProvider {

    public MaterialPipeBlockItem(MaterialPipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    @Nonnull
    public MaterialPipeBlock getBlock() {
        return (MaterialPipeBlock)super.getBlock();
    }

    public static int tintColor(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof MaterialPipeBlockItem materialBlockItem) {
            return materialBlockItem.getBlock().tinted(materialBlockItem.getBlock().defaultBlockState(), null, null, index);
        }
        return -1;
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return getBlock().getRenderer(getBlock().defaultBlockState());
    }

}
