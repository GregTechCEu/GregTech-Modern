package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.SpriteOverrider;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.ibm.icu.impl.CurrencyData.provider;

@Getter
public abstract class TextureOverrideRenderer extends BaseBakedModel {

    protected ResourceLocation modelLocation;
    @Nullable
    private BakedModel baseModel;

    @OnlyIn(Dist.CLIENT)
    protected Map<ModelState, BakedModel> modelCaches;

    @NotNull
    protected Map<String, ResourceLocation> override;
    @Nullable
    protected Supplier<Map<String, ResourceLocation>> overrideSupplier;
    @OnlyIn(Dist.CLIENT)
    protected Map<ModelState, BakedModel> bakedModelCache;

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

    @Override
    public void initRenderer() {
        if (GTCEu.isClientSide()) {
            this.bakedModelCache = new ConcurrentHashMap<>();
        }
        super.initRenderer();
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

    public UnbakedModel getModel() {
        return Minecraft.getInstance().getModelManager().getModelBakery().getModel(modelLocation);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
                                             @NotNull ModelData extraData, @Nullable RenderType renderType) {
        return super.getQuads(state, side, rand);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected BakedModel getBlockBakedModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state) {
        var modelState = provider.getModelState(level, pos, state);
        if (modelState != null) {
            return modelCaches.computeIfAbsent(modelState, ms -> getModel().bake(
                    ModelFactory.getModelBaker(),
                    this::materialMapping,
                    ms));
        }
        return modelCaches.computeIfAbsent(BlockModelRotation.X0_Y0, ms -> getModel().bake(
                ModelFactory.getModelBaker(),
                this::materialMapping,
                ms));
    }

    @OnlyIn(Dist.CLIENT)
    public BakedModel getRotatedModel(Direction frontFacing) {
        return blockModels.computeIfAbsent(frontFacing, facing -> getModel().bake(
                ModelFactory.getModeBaker(),
                new SpriteOverrider(override),
                ModelFactory.getRotation(facing),
                modelLocation));
    }

    @OnlyIn(Dist.CLIENT)
    public BakedModel getRotatedModel(ModelState modelState) {
        return bakedModelCache.computeIfAbsent(modelState, state -> getModel().bake(
                ModelFactory.getModeBaker(),
                new SpriteOverrider(override),
                modelState,
                modelLocation));
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) { // prepare for override.
            if (bakedModelCache != null) {
                bakedModelCache.clear();
            }
            if (overrideSupplier != null) override = overrideSupplier.get();
            for (ResourceLocation value : override.values()) {
                register.accept(value);
            }
        }
    }

    @Override
    public void onAdditionalModel(Consumer<ResourceLocation> consumer) {
        super.onAdditionalModel(consumer);
    }

    @Override
    public void updateModelWithoutReloadingResource(ResourceLocation modelLocation) {
        super.updateModelWithoutReloadingResource(modelLocation);
        if (LDLib.isClient()) {
            if (bakedModelCache != null) {
                bakedModelCache.clear();
            }
        }
    }
}
