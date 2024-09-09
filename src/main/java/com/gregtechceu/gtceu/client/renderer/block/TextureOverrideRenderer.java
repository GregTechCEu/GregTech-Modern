package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.client.model.SpriteOverrider;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public abstract class TextureOverrideRenderer extends BaseBakedModel {

    protected ResourceLocation modelLocation;
    @Nullable
    private BakedModel baseModel;
    @NotNull
    protected Map<String, ResourceLocation> override;
    @Nullable
    protected Supplier<Map<String, ResourceLocation>> overrideSupplier;

    public TextureOverrideRenderer(ResourceLocation model, @NotNull Map<String, ResourceLocation> override) {
        this.modelLocation = model;
        this.override = override;
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    public TextureOverrideRenderer(ResourceLocation model,
                                   @NotNull Supplier<Map<String, ResourceLocation>> overrideSupplier) {
        this.modelLocation = model;
        this.override = Collections.emptyMap();
        this.overrideSupplier = overrideSupplier;
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    public TextureOverrideRenderer(ResourceLocation model) {
        this.modelLocation = model;
        this.override = Collections.emptyMap();
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    public void setTextureOverride(Map<String, ResourceLocation> override) {
        this.override = override;
    }

    public Map<String, ResourceLocation> getTextureOverride() {
        if (override.isEmpty() && overrideSupplier != null) {
            override = overrideSupplier.get();
        }
        return override;
    }

    public BakedModel getBaseModel() {
        if (this.baseModel == null) {
            this.baseModel = ModelFactory.getModeBakery()
                    .getModel(modelLocation)
                    .bake(ModelFactory.getModeBaker(), new SpriteOverrider(getTextureOverride()),
                            BlockModelRotation.X0_Y0, modelLocation);
        }
        return this.baseModel;
    }

    @Override
    public void onAdditionalModel(Consumer<ResourceLocation> consumer) {
        super.onAdditionalModel(consumer);
    }
}
