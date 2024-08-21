package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import gregtech.api.unification.material.Material;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ItemMaterialPipeBlock extends ItemPipeBlock {

    public ItemMaterialPipeBlock(PipeMaterialBlock block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public @NotNull PipeMaterialBlock getBlock() {
        return (PipeMaterialBlock) super.getBlock();
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack stack) {
        Material material = getBlock().getMaterialForStack(stack);
        return material == null ? "unnamed" : getBlock().getStructure().getOrePrefix().getLocalNameForItem(material);
    }
}
