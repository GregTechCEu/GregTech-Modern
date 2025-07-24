package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;

@SuppressWarnings("unused")
public class ActiveBlockBuilder extends BlockBuilder {

    private enum Type {
        SIMPLE,
        BLOOM,
        FIREBOX,
        CUSTOM
    }

    private transient Type type = Type.CUSTOM;

    public ActiveBlockBuilder(ResourceLocation id) {
        super(id);
        property(GTBlockStateProperties.ACTIVE);
        renderType("cutout_mipped");
    }

    public ActiveBlockBuilder simple(String base) {
        textures.addProperty("base", base);
        textures.addProperty("active", base + "_active");
        type = Type.SIMPLE;
        return this;
    }

    public ActiveBlockBuilder firebox(String bottom, String side, String top) {
        textures.addProperty("bottom", bottom);
        textures.addProperty("side", side);
        textures.addProperty("top", top);
        type = Type.FIREBOX;
        return this;
    }

    public ActiveBlockBuilder bloom(String base) {
        textures.addProperty("base", base);
        textures.addProperty("bloom", base + "_bloom");
        type = Type.BLOOM;
        return this;
    }

    @Override
    protected void generateBlockStateJson(VariantBlockStateGenerator bs) {
        bs.simpleVariant("active=false", newID("block/", "").toString());
        bs.simpleVariant("active=true", newID("block/", "_active").toString());
    }

    @Override
    protected void generateBlockModelJsons(AssetJsonGenerator generator) {
        var activeID = id.withSuffix("_active");
        if (type == Type.SIMPLE) {
            final String base = textures.get("base").getAsString();
            final String active = textures.get("active").getAsString();
            generator.blockModel(id, m -> {
                m.parent("minecraft:block/cube_all");
                m.texture("all", base);
            });
            generator.blockModel(activeID, m -> {
                m.parent("minecraft:block/cube_all");
                m.texture("all", active);
            });
        } else if (type == Type.FIREBOX) {
            final String bottom = textures.get("bottom").getAsString();
            final String side = textures.get("side").getAsString();
            final String top = textures.get("top").getAsString();
            generator.blockModel(id, m -> {
                m.parent("minecraft:block/cube_bottom_top");
                m.texture("bottom", bottom);
                m.texture("side", side);
                m.texture("top", top);
            });
            generator.blockModel(activeID, m -> {
                m.parent("gtceu:block/fire_box_active");
                m.texture("bottom", bottom);
                m.texture("side", side);
                m.texture("top", top);
            });
        } else if (type == Type.BLOOM) {
            final String base = textures.get("base").getAsString();
            final String bloom = textures.get("bloom").getAsString();
            generator.blockModel(id, m -> {
                m.parent("minecraft:block/cube_all");
                m.texture("all", base);
            });
            generator.blockModel(activeID, m -> {
                m.parent("gtceu:block/cube_2_layer/all");
                m.texture("bot_all", base);
                m.texture("top_all", bloom);
            });
        }
    }

    @Override
    public Block createObject() {
        return new ActiveBlock(createProperties());
    }
}
