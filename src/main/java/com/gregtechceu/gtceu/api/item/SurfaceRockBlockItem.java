package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.block.SurfaceRockBlock;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

public class SurfaceRockBlockItem extends BlockItem {

    @Getter
    private Material mat;

    public SurfaceRockBlockItem(SurfaceRockBlock block, Properties props, Material mat) {
        super(block, props);
        this.mat = mat;
    }

    public static SurfaceRockBlockItem create(SurfaceRockBlock block, Properties props, Material mat) {
        return new SurfaceRockBlockItem(block, props, mat);
    }

    @Override
    public SurfaceRockBlock getBlock() {
        return (SurfaceRockBlock) super.getBlock();
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
