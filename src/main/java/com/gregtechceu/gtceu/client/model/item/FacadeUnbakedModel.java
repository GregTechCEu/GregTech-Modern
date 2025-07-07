package com.gregtechceu.gtceu.client.model.item;

import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FacadeUnbakedModel implements IUnbakedGeometry<FacadeUnbakedModel> {

    private final @NotNull BlockModel defaultModel;

    private FacadeUnbakedModel(@NotNull BlockModel defaultModel) {
        this.defaultModel = defaultModel;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel bakedParent = defaultModel.bake(baker, defaultModel, spriteGetter, modelState, modelLocation, true);
        return new FacadeCoverRenderer(bakedParent);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                               IGeometryBakingContext context) {
        defaultModel.resolveParents(modelGetter);
    }

    public static class Loader implements IGeometryLoader<FacadeUnbakedModel> {

        public static final Loader INSTANCE = new Loader();

        protected Loader() {}

        @Override
        public FacadeUnbakedModel read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
            return new FacadeUnbakedModel(context.deserialize(json.get("default_model"), BlockModel.class));
        }
    }
}
