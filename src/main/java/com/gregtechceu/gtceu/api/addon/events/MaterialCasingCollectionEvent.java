package com.gregtechceu.gtceu.api.addon.events;

import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialEntry;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.util.entry.BlockEntry;

public class MaterialCasingCollectionEvent extends Event implements IModBusEvent {

    private final ImmutableMap.Builder<MaterialEntry, BlockEntry<Block>> builder;

    public MaterialCasingCollectionEvent(ImmutableMap.Builder<MaterialEntry, BlockEntry<Block>> builder) {
        this.builder = builder;
    }

    public void add(MaterialEntry material, BlockEntry<Block> casingBlock) {
        builder.put(material, casingBlock);
    }
}
