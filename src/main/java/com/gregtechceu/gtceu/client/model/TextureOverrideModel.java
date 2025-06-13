package com.gregtechceu.gtceu.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gregtechceu.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextureOverrideModel implements IUnbakedGeometry<TextureOverrideModel> {

    @NotNull
    @Getter
    @Setter
    protected Map<String, ResourceLocation> textureOverride;
    @Nullable
    @Getter
    protected Supplier<Map<String, ResourceLocation>> overrideSupplier;

    public TextureOverrideModel(@NotNull Map<String, ResourceLocation> textureOverride) {
        this.textureOverride = textureOverride;
        this.overrideSupplier = null;
    }

    public TextureOverrideModel(@NotNull Supplier<Map<String, ResourceLocation>> overrideSupplier) {
        this.overrideSupplier = overrideSupplier;
        this.textureOverride = Collections.emptyMap();
    }

    public Map<String, ResourceLocation> getTextureOverride() {
        if (textureOverride.isEmpty() && overrideSupplier != null) {
            textureOverride = overrideSupplier.get();
        }
        return textureOverride;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        if (context instanceof BlockGeometryBakingContext blockCtx) {
            UnbakedModel parent = blockCtx.owner.parent;
            if (parent == null) return null;
            ResourceLocation parentLoc = blockCtx.owner.getParentLocation();
            return parent.bake(baker, new SpriteOverrider(getTextureOverride(), spriteGetter), modelState, parentLoc);
        }

        return null;
    }

    public static class Loader implements IGeometryLoader<TextureOverrideModel> {

        public static final Loader INSTANCE = new Loader();

        protected Loader() {}

        @Override
        public TextureOverrideModel read(JsonObject jsonObject,
                                         JsonDeserializationContext deserializationContext) throws JsonParseException {
            Map<String, ResourceLocation> overrides = new HashMap<>();
            if (jsonObject.has("override")) {
                JsonObject overrideJson = GsonHelper.getAsJsonObject(jsonObject, "override");
                for (var entry : overrideJson.entrySet()) {
                    ResourceLocation textureLoc = ResourceLocation.tryParse(entry.getValue().getAsString());
                    if (textureLoc == null) {
                        throw new JsonParseException(entry.getValue() + " is not valid resource location");
                    }
                    overrides.put(entry.getKey(), textureLoc);
                }
            }
            return new TextureOverrideModel(overrides);
        }
    }

    public static class Baked extends BaseBakedModel {

        @OnlyIn(Dist.CLIENT)
        @Getter
        protected Map<ModelState, BakedModel> modelCaches;

        protected BakedModel parentModel;

        public Baked(BakedModel parent) {
            this.parentModel = parent;
            if (GTCEu.isClientSide()) {
                registerEvent();
            }
            initRenderer();
        }

        public void initRenderer() {
            if (GTCEu.isClientSide()) {
                this.bakedModelCache = new ConcurrentHashMap<>();
            }
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                                 @NotNull RandomSource rand,
                                                 @NotNull ModelData extraData, @Nullable RenderType renderType) {
            return super.getQuads(state, side, rand);
        }

        @OnlyIn(Dist.CLIENT)
        @Nullable
        protected BakedModel getBlockBakedModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state) {
            ModelState modelState = null;
            if (modelState != null) {
                return modelCaches.computeIfAbsent(modelState, ms -> getModel().bake(
                        getModelBaker(),
                        BaseBakedModel::spriteGetter,
                        ms, modelLocation));
            }
            return modelCaches.computeIfAbsent(BlockModelRotation.X0_Y0, ms -> getModel().bake(
                    getModelBaker(),
                    BaseBakedModel::spriteGetter,
                    ms, modelLocation));
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public BakedModel getRotatedModel(ModelState modelState) {
            return bakedModelCache.computeIfAbsent(modelState, state -> getModel().bake(
                    ModelFactory.getModeBaker(),
                    new SpriteOverrider(textureOverride),
                    modelState,
                    modelLocation));
        }

        @Override
        public BakedModel getBaseModel() {
            if (this.baseModel == null) {
                this.baseModel = getModelBakery().getModel(modelLocation)
                        .bake(getModelBaker(), new SpriteOverrider(getTextureOverride()),
                                BlockModelRotation.X0_Y0, modelLocation);
            }
            return this.baseModel;
        }

        // @Override
        // @OnlyIn(Dist.CLIENT)
        // public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        //     if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) { // prepare for override.
        //         if (bakedModelCache != null) {
        //             bakedModelCache.clear();
        //         }
        //         if (overrideSupplier != null) override = overrideSupplier.get();
        //         for (ResourceLocation value : override.values()) {
        //             register.accept(value);
        //         }
        //     }
        // }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return true;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public @NotNull ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }

    }


}
