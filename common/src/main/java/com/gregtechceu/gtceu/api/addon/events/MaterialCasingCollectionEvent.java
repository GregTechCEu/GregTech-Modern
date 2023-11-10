package com.gregtechceu.gtceu.api.addon.events;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;

public class MaterialCasingCollectionEvent {
    private final ImmutableMap.Builder<Material, BlockEntry<Block>> builder;

    public MaterialCasingCollectionEvent(ImmutableMap.Builder<Material, BlockEntry<Block>> builder) {
        this.builder = builder;
    }

    public void add(Material material, BlockEntry<Block> casingBlock) {
        builder.put(material, casingBlock);
    }
}
