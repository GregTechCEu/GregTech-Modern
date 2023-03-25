package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.core.mixins.BlockModelAccessor;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/25
 * @implNote CoilRenderer
 */
public class TextureOverrideRenderer extends CTMModelRenderer {
    private final Map<String, Object> override;

    public TextureOverrideRenderer(ResourceLocation model, Map<String, Object> override) {
        super(model);
        this.override = override;
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected UnbakedModel getModel() {
        var model = super.getModel();
        if (model instanceof BlockModelAccessor blockModelAccessor) {
            override.forEach((key, value) -> blockModelAccessor.getTextureMap().put(key, ModelFactory.parseBlockTextureLocationOrReference(value.toString())));
        }
        return model;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) { // prepare for override.
            for (Object value : override.values()) {
                register.accept(new ResourceLocation(value.toString()));
            }
        }
    }
}
