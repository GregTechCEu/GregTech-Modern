package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.utils.fakelevel.*;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.*;
import lombok.experimental.Accessors;

import java.util.function.*;

@Accessors(fluent = true, chain = true)
public class SchemaRenderer extends BaseSchemaRenderer {

    public SchemaRenderer(ISchema schema, RenderTarget renderTarget) {
        super(schema, renderTarget);
    }

    public SchemaRenderer(ISchema schema) {
        super(schema);
    }

    public SchemaRenderer afterRender(Runnable consumer) {
        this.afterRender = consumer;
        return this;
    }

    public SchemaRenderer reyTracing(Consumer rayTracing) {
        this.onRayTrace = rayTracing;
        return this;
    }

    public SchemaRenderer cameraFunc(BiConsumer<Camera, ISchema> camera) {
        this.cameraFunc = camera;
        return this;
    }

    public SchemaRenderer isometric(Boolean isometric) {
        this.isometric = isometric;
        return this;
    }

    public SchemaRenderer scale(double scale) {
        this.scale = () -> scale;
        return this;
    }

    public SchemaRenderer scale(DoubleSupplier scale) {
        this.scale = scale;
        return this;
    }

    public SchemaRenderer disableBER(boolean disableBER) {
        this.disableBER = () -> disableBER;
        return this;
    }

    public SchemaRenderer disableBER(BooleanSupplier disableBER) {
        this.disableBER = disableBER;
        return this;
    }

    public SchemaRenderer highlightRenderer(BlockHighlight supp) {
        this.highlight = () -> supp;
        return this;
    }
}
