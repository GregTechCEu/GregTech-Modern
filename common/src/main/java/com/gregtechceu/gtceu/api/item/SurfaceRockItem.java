package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.common.block.SurfaceRockBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SurfaceRockItem extends BlockItem {
    public SurfaceRockItem(SurfaceRockBlock block, Properties properties) {
        super(block, properties);
    }

    public void onRegister() {

    }

    @Override
    @Nonnull
    public SurfaceRockBlock getBlock() {
        return (SurfaceRockBlock)super.getBlock();
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
