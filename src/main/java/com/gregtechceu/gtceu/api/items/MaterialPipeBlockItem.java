package com.gregtechceu.gtceu.api.items;

import com.gregtechceu.gtceu.api.blocks.MaterialPipeBlock;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @implNote MaterialBlockItem
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MaterialPipeBlockItem extends PipeBlockItem implements IItemRendererProvider {

    public MaterialPipeBlockItem(MaterialPipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    @NotNull
    public MaterialPipeBlock getBlock() {
        return (MaterialPipeBlock)super.getBlock();
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof MaterialPipeBlockItem materialBlockItem) {
                return materialBlockItem.getBlock().tinted(materialBlockItem.getBlock().defaultBlockState(), null, null, index);
            }
            return -1;
        };
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRenderer getRenderer(ItemStack stack) {
        return getBlock().getRenderer(getBlock().defaultBlockState());
    }

    @Override
    public Component getDescription() {
        return this.getBlock().getName();
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription();
    }
}
