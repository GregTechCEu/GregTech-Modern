package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class TextureOverrideModel implements IUnbakedGeometry<TextureOverrideModel> {

    @Getter
    private final BlockModel child;
    protected @NotNull Map<String, ResourceLocation> textureOverride;

    public TextureOverrideModel(@NotNull Map<String, ResourceLocation> textureOverride,
                                BlockModel child) {
        this.textureOverride = textureOverride;
        this.child = child;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        child.resolveParents(modelGetter);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @Nullable BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                                     Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                                     ItemOverrides overrides, ResourceLocation modelLocation) {
        Map<TextureAtlasSprite, TextureAtlasSprite> textures = new HashMap<>();
        for (var entry : this.textureOverride.entrySet()) {
            Material key = context.getMaterial(entry.getKey());
            Material value = new Material(TextureAtlas.LOCATION_BLOCKS, entry.getValue());
            textures.put(spriteGetter.apply(key), spriteGetter.apply(value));
        }

        BakedModel bakedChild = this.child.bake(baker, this.child, spriteGetter, modelState, modelLocation, true);
        return new BakedTextureOverrideModel<>(bakedChild, textures);
    }

    public static class Loader implements IGeometryLoader<TextureOverrideModel> {

        public static final Loader INSTANCE = new Loader();
        public static final ResourceLocation ID = GTCEu.id("texture_override");

        private Loader() {}

        @Override
        public TextureOverrideModel read(JsonObject json,
                                         JsonDeserializationContext context) throws JsonParseException {
            Map<String, ResourceLocation> overrides = new HashMap<>();
            if (json.has("override")) {
                JsonObject overrideJson = GsonHelper.getAsJsonObject(json, "override");
                for (var entry : overrideJson.entrySet()) {
                    ResourceLocation textureLoc = ResourceLocation.tryParse(entry.getValue().getAsString());
                    if (textureLoc == null) {
                        throw new JsonParseException(entry.getValue() + " is not valid resource location");
                    }
                    overrides.put(entry.getKey(), textureLoc);
                }
            }
            return new TextureOverrideModel(overrides, context.deserialize(json.get("child"), BlockModel.class));
        }
    }
}
