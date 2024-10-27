package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.common.block.LaserPipeBlock;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LaserPipeBlockItem extends PipeBlockItem implements IItemRendererProvider {

    public LaserPipeBlockItem(PipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public LaserPipeBlock getBlock() {
        return (LaserPipeBlock) super.getBlock();
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof LaserPipeBlockItem materialBlockItem) {
                return LaserPipeBlock.tintedColor().getColor(materialBlockItem.getBlock().defaultBlockState(), null,
                        null, index);
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
}
