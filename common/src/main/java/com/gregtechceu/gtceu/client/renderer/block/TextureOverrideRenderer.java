package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.client.model.SpriteOverrider;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/3/25
 * @implNote CoilRenderer
 */
@Getter
public class TextureOverrideRenderer extends CTMModelRenderer {

    @Nonnull
    protected Map<String, ResourceLocation> override;
    @Nullable
    protected Supplier<Map<String, ResourceLocation>> overrideSupplier;

    public TextureOverrideRenderer(ResourceLocation model, @Nonnull Map<String, ResourceLocation> override) {
        super(model);
        this.override = override;
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    public TextureOverrideRenderer(ResourceLocation model, @Nonnull Supplier<Map<String, ResourceLocation>> overrideSupplier) {
        super(model);
        this.override = Collections.emptyMap();
        this.overrideSupplier = overrideSupplier;
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    public TextureOverrideRenderer(ResourceLocation model) {
        super(model);
        this.override = Collections.emptyMap();
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    public void setTextureOverride(Map<String, ResourceLocation> override) {
        this.override = override;
    }

    @Nullable
    @Environment(EnvType.CLIENT)
    protected BakedModel getItemBakedModel() {
        if (itemModel == null) {
            var model = getModel();
            if (model instanceof BlockModel blockModel && blockModel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                // fabric doesn't help us to fix vanilla bakery, so we have to do it ourselves
                model = ModelFactory.ITEM_MODEL_GENERATOR.generateBlockModel(new SpriteOverrider(override), blockModel);
            }
            itemModel = model.bake(
                    ModelFactory.getModeBaker(),
                    new SpriteOverrider(override),
                    BlockModelRotation.X0_Y0,
                    modelLocation);
        }
        return itemModel;
    }

    @Environment(EnvType.CLIENT)
    public BakedModel getRotatedModel(Direction frontFacing) {
        return blockModels.computeIfAbsent(frontFacing, facing -> getModel().bake(
                ModelFactory.getModeBaker(),
                new SpriteOverrider(override),
                ModelFactory.getRotation(facing),
                modelLocation));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) { // prepare for override.
            if (overrideSupplier != null) override = overrideSupplier.get();
            for (Object value : override.values()) {
                register.accept(new ResourceLocation(value.toString()));
            }
        }
    }
}
