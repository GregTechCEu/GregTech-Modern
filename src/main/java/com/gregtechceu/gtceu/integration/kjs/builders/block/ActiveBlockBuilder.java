package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;

public class ActiveBlockBuilder extends BlockBuilder {

    public ActiveBlockBuilder(ResourceLocation id) {
        super(id);
        property(ActiveBlock.ACTIVE);
    }

    public ActiveBlockBuilder baseTexture(String tex) {
        textures.addProperty("base", tex);
        return this;
    }

    @Override
    protected void generateBlockStateJson(VariantBlockStateGenerator bs) {
        bs.simpleVariant("active=false", newID("block/variant/", "").toString());
        bs.simpleVariant("active=true", newID("block/variant/", "_active").toString());
    }

    @Override
    protected void generateBlockModelJsons(AssetJsonGenerator generator) {
        final var base = textures.get("base").getAsString();
        generator.blockModel(newID("variant/", ""), m -> {
            m.parent("minecraft:block/cube_all");
            m.texture("all", base);
        });

        generator.blockModel(newID("variant/", "_active"), m -> {
            m.parent("minecraft:block/cube_all");
            m.texture("all", base + "_active");
        });
    }

    @Override
    protected void generateItemModelJson(ModelGenerator m) {
        m.parent(model.isEmpty() ? newID("block/variant/", "").toString() : model);
    }

    @Override
    public Block createObject() {
        return new ActiveBlock(createProperties());
    }
}
