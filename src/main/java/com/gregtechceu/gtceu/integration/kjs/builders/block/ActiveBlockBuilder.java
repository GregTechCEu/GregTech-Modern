package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.BlockRenderType;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.util.ID;

import java.util.function.UnaryOperator;

import static dev.latvian.mods.kubejs.generator.KubeAssetGenerator.*;

public class ActiveBlockBuilder extends BlockBuilder {

    // spotless:off
    public static final UnaryOperator<String> ACTIVE = s -> s + "_active";
    public static final UnaryOperator<String> BLOOM = s -> s + "_bloom";
    public static final UnaryOperator<String> ACTIVE_BLOCK = (UnaryOperator<String>) ID.BLOCK.andThen(ACTIVE);
    public static final ResourceLocation CUBE_2_LAYER_ALL_MODEL = GTCEu.id("block/cube_2_layer/all");
    public static final ResourceLocation CUBE_BOTTOM_TOP_MODEL = ResourceLocation.withDefaultNamespace("block/cube_bottom_top");
    public static final ResourceLocation FIRE_BOX_ACTIVE_MODEL = GTCEu.id("block/fire_box_active");
    // spotless:on

    public enum Type {
        SIMPLE,
        BLOOM,
        FIREBOX,
        CUSTOM
    }

    public transient Type type = Type.CUSTOM;
    public transient String activeTexture;

    public ActiveBlockBuilder(ResourceLocation id) {
        super(id);
        property(GTBlockStateProperties.ACTIVE);
        renderType(BlockRenderType.CUTOUT_MIPPED);
        activeTexture = ACTIVE.apply(baseTexture);
    }

    public ActiveBlockBuilder simple(String base) {
        baseTexture = base;
        activeTexture = ACTIVE.apply(base);
        type = Type.SIMPLE;
        return this;
    }

    public ActiveBlockBuilder firebox(String bottom, String side, String top) {
        textures.put("bottom", bottom);
        textures.put("side", side);
        textures.put("top", top);
        type = Type.FIREBOX;
        return this;
    }

    public ActiveBlockBuilder bloom(String base) {
        baseTexture = base;
        activeTexture = BLOOM.apply(base);
        type = Type.BLOOM;
        return this;
    }

    @Override
    protected void generateBlockState(VariantBlockStateGenerator bs) {
        bs.simpleVariant("active=false", id.withPath(ID.BLOCK));
        bs.simpleVariant("active=true", id.withPath(ACTIVE_BLOCK));
    }

    @Override
    protected void generateBlockModels(KubeAssetGenerator generator) {
        if (type == Type.CUSTOM || modelGenerator != null) {
            superGenerateBlockModels(generator);
            return;
        }
        generator.blockModel(id, m -> {
            if (type == Type.SIMPLE || type == Type.BLOOM) {
                m.parent(CUBE_ALL_BLOCK_MODEL);
                m.texture("all", baseTexture);
            } else if (type == Type.FIREBOX) {
                m.parent(CUBE_BOTTOM_TOP_MODEL);
                m.textures(textures);
            }
        });
        generator.blockModel(id.withPath(ACTIVE), m -> {
            if (type == Type.SIMPLE) {
                m.parent(CUBE_ALL_BLOCK_MODEL);
                m.texture("all", activeTexture);
            } else if (type == Type.BLOOM) {
                m.parent(CUBE_2_LAYER_ALL_MODEL);
                m.texture("bot_all", baseTexture);
                m.texture("top_all", activeTexture);
            } else if (type == Type.FIREBOX) {
                m.parent(FIRE_BOX_ACTIVE_MODEL);
                m.textures(textures);
            }
        });
    }

    protected void superGenerateBlockModels(KubeAssetGenerator generator) {
        super.generateBlockModels(generator);
    }

    @Override
    public Block createObject() {
        return new ActiveBlock(createProperties());
    }
}
