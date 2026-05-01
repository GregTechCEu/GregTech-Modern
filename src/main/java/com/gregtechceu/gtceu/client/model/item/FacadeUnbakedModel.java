package com.gregtechceu.gtceu.client.model.item;

import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.function.Function;

public class FacadeUnbakedModel implements IUnbakedGeometry<FacadeUnbakedModel> {

    private FacadeUnbakedModel() {}

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        return new FacadeCoverRenderer(null);
    }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> modelGetter,
                               IGeometryBakingContext context) {}

    public static class Loader implements IGeometryLoader<FacadeUnbakedModel> {

        public static final Loader INSTANCE = new Loader();

        protected Loader() {}

        @Override
        public FacadeUnbakedModel read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
            // The legacy `default_model` field is intentionally ignored — FacadeCoverRenderer
            // builds its own quads at runtime, so the inline default model JSON was never used
            // (and 26.1.2 dropped the BlockModel class that the deserializer relied on).
            return new FacadeUnbakedModel();
        }
    }
}
