package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.api.block.RendererBlock;
import com.gregtechceu.gtceu.client.renderer.block.CTMModelRenderer;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.integration.kjs.builders.RendererBlockItemBuilder;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
@Accessors(chain = true, fluent = true)
public class RendererBlockBuilder extends BlockBuilder {

    @Setter
    public transient IRenderer renderer;

    public RendererBlockBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {}

    public RendererBlockBuilder textureOverrideRenderer(ResourceLocation modelPath,
                                                        Map<String, ResourceLocation> textures) {
        this.renderer = new TextureOverrideRenderer(modelPath, textures);
        return this;
    }

    public RendererBlockBuilder ctmRenderer(ResourceLocation modelPath) {
        this.renderer = new CTMModelRenderer(modelPath);
        return this;
    }

    @Override
    protected ItemBuilder getOrCreateItemBuilder() {
        return itemBuilder == null ? (itemBuilder = new RendererBlockItemBuilder(id)) : itemBuilder;
    }

    @Override
    public Block createObject() {
        return new RendererBlock(this.createProperties(), renderer);
    }
}
