package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import gregtech.api.unification.material.Material;

import net.minecraft.item.ItemStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MaterialPipeBlockItem extends PipeBlockItem {

    public MaterialPipeBlockItem(Item.Properties properties, PipeMaterialBlock block) {
        super(block, properties);
    }

    @Override
    public @NotNull PipeMaterialBlock getBlock() {
        return (PipeMaterialBlock) super.getBlock();
    }

    @Override
    public Component getName(ItemStack stack) {
        Material material = getBlock().getMaterialForStack(stack);
        return material == null ? "unnamed" : getBlock().getStructure().getTagPrefix().getLocalNameForItem(material);
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack stack) {
        Material material = getBlock().getMaterialForStack(stack);
        return material == null ? "unnamed" : getBlock().getStructure().getTagPrefix().getLocalNameForItem(material);
    }
}
