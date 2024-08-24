package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class MaterialPipeBlockItem extends PipeBlockItem {

    public MaterialPipeBlockItem(PipeMaterialBlock block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull PipeMaterialBlock getBlock() {
        return (PipeMaterialBlock) super.getBlock();
    }

    @Override
    public Component getName(ItemStack stack) {
        Material material = getBlock().material;
        return material == null ? Component.literal("unnamed") :
                getBlock().getStructure().getPrefix().getLocalizedName(material);
    }

    @Override
    public Component getDescription() {
        Material material = getBlock().material;
        return material == null ? Component.literal("unnamed") :
                getBlock().getStructure().getPrefix().getLocalizedName(material);
    }

    @Override
    public String getDescriptionId() {
        Material material = getBlock().material;
        return material == null ? "unnamed" :
                getBlock().getStructure().getPrefix().getUnlocalizedName(material);
    }
}
