package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.core.mixins.BlockModelAccessor;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.ElementsModel;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextureOverrideModel implements IUnbakedGeometry<TextureOverrideModel> {

    @NotNull
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
        spriteGetter = new SpriteOverrider(getTextureOverride(), spriteGetter);

        if (context instanceof BlockGeometryBakingContext blockCtx) {
            BlockModel model = blockCtx.owner;
            if (model == null) return null;
            // replicate UnbakedGeometryHelper's default logic
            var elementsModel = new ElementsModel(((BlockModelAccessor) model).gtceu$getRawElements());
            return elementsModel.bake(blockCtx, baker, spriteGetter, modelState,
                    model.getOverrides(baker, model, spriteGetter), modelLocation);
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
}
