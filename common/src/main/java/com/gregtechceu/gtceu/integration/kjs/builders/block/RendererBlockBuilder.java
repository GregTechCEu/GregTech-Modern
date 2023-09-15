package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.api.block.RendererBlock;
import com.gregtechceu.gtceu.client.renderer.block.CTMModelRenderer;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.integration.kjs.builders.RendererBlockItemBuilder;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.BlockItemBuilder;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

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
    protected void generateItemModelJson(ModelGenerator m) {

    }

    @Override
    protected void generateBlockModelJsons(AssetJsonGenerator generator) {

    }

    public RendererBlockBuilder textureOverrideRenderer(ResourceLocation modelPath, Map<String, ResourceLocation> textures) {
        this.renderer = new TextureOverrideRenderer(modelPath, textures);
        return this;
    }

    public RendererBlockBuilder ctmRenderer(ResourceLocation modelPath) {
        this.renderer = new CTMModelRenderer(modelPath);
        return this;
    }

    @Override
    protected BlockItemBuilder getOrCreateItemBuilder() {
        return itemBuilder == null ? (itemBuilder = new RendererBlockItemBuilder(id)) : itemBuilder;
    }

    @Override
    public Block createObject() {
        return new RendererBlock(this.createProperties(), renderer);
    }
}
