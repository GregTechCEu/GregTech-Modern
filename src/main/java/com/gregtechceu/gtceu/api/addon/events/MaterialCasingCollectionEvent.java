package com.gregtechceu.gtceu.api.addon.events;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.util.entry.BlockEntry;

public class MaterialCasingCollectionEvent extends Event implements IModBusEvent {

    private final ImmutableMap.Builder<Material, BlockEntry<Block>> builder;

    public MaterialCasingCollectionEvent(ImmutableMap.Builder<Material, BlockEntry<Block>> builder) {
        this.builder = builder;
    }

    public void add(Material material, BlockEntry<Block> casingBlock) {
        builder.put(material, casingBlock);
    }
}
