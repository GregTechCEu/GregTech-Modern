package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

public class MaterialPipeBlockItem extends PipeBlockItem {

    public MaterialPipeBlockItem(MaterialPipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    @NotNull
    public MaterialPipeBlock getBlock() {
        return (MaterialPipeBlock) super.getBlock();
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof MaterialPipeBlockItem materialBlockItem) {
                return materialBlockItem.getBlock().tinted(materialBlockItem.getBlock().defaultBlockState(), null, null,
                        index);
            }
            return -1;
        };
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
