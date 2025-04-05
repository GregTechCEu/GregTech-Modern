package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.SpriteOverrider;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import com.mojang.math.Transformation;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public abstract class TextureOverrideRenderer extends BaseBakedModel {

    protected ResourceLocation modelLocation;
    @Nullable
    private BakedModel baseModel;
    @NotNull
    protected Map<String, ResourceLocation> override;
    @Nullable
    protected Supplier<Map<String, ResourceLocation>> overrideSupplier;
    protected Transformation transformation = null;
    protected BakedModel cachedModel = null;

    public TextureOverrideRenderer(ResourceLocation model, @NotNull Map<String, ResourceLocation> override) {
        this.modelLocation = model;
        this.override = override;
        if (GTCEu.isClientSide()) {
            registerEvent();
        }
    }

    public TextureOverrideRenderer(ResourceLocation model,
                                   @NotNull Supplier<Map<String, ResourceLocation>> overrideSupplier) {
        this.modelLocation = model;
        this.override = Collections.emptyMap();
        this.overrideSupplier = overrideSupplier;
        if (GTCEu.isClientSide()) {
            registerEvent();
        }
    }

    public TextureOverrideRenderer(ResourceLocation model) {
        this.modelLocation = model;
        this.override = Collections.emptyMap();
        if (GTCEu.isClientSide()) {
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
                    .bake(ModelFactory.getModeBaker(),
                            new SpriteOverrider(getTextureOverride()),
                            BlockModelRotation.X0_Y0,
                            modelLocation);
        }
        return this.baseModel;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAdditionalModel(Consumer<ResourceLocation> consumer) {
        super.onAdditionalModel(consumer);
    }
}
