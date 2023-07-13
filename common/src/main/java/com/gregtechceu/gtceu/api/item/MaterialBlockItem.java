package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @implNote MaterialBlockItem
 */
public class MaterialBlockItem extends BlockItem implements IItemRendererProvider {

    public MaterialBlockItem(MaterialBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    @Nonnull
    public MaterialBlock getBlock() {
        return (MaterialBlock)super.getBlock();
    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof MaterialBlockItem materialBlockItem) {
                return materialBlockItem.getBlock().material.getMaterialARGB();
            }
            return -1;
        };
    }

    @Nullable
    @Override
    @Environment(EnvType.CLIENT)
    public IRenderer getRenderer(ItemStack stack) {
        return getBlock().getRenderer(getBlock().defaultBlockState());
    }

    @Override
    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }

    @Override
    public Component getDescription() {
        return getBlock().getName();
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription();
    }
}
