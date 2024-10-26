package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.item.RendererBlockItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import dev.latvian.mods.kubejs.block.BlockItemBuilder;

public class RendererBlockItemBuilder extends BlockItemBuilder {

    public RendererBlockItemBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public Item createObject() {
        return new RendererBlockItem(blockBuilder.get(), createItemProperties());
    }
}
