package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.common.block.DuctPipeBlock;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DuctPipeBlockItem extends PipeBlockItem implements IItemRendererProvider {

    public DuctPipeBlockItem(DuctPipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    @NotNull
    public DuctPipeBlock getBlock() {
        return (DuctPipeBlock) super.getBlock();
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return getBlock().getRenderer(getBlock().defaultBlockState());
    }
}
