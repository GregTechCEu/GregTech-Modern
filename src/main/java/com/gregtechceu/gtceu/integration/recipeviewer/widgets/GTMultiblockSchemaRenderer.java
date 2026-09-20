package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;

import net.minecraft.client.renderer.RenderType;

import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.schema.ISchema;

public class GTMultiblockSchemaRenderer extends SchemaRenderer {

    public GTMultiblockSchemaRenderer(ISchema schema) {
        super(schema);
    }

    @Override
    protected void renderBlocks(RenderCompileResults renderResult, RenderType renderType) {
        super.renderBlocks(renderResult, renderType);
        if (renderType != RenderType.cutout()) return;

        for (var pass : CustomChunkRenderPassRegistry.afterCutoutPasses()) {
            super.renderBlocks(renderResult, pass.renderType());
        }
    }
}
