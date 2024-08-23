package com.gregtechceu.gtceu.client.renderer.pipe;

import net.minecraft.client.renderer.block.model.ItemOverrides;
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
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class UnbakedPipeModel implements IUnbakedGeometry<UnbakedPipeModel> {

    private final AbstractPipeModel<?> model;

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> function, ModelState arg2, ItemOverrides arg3,
                           ResourceLocation arg4) {
        return model;
    }

    public static final class Loader implements IGeometryLoader<UnbakedPipeModel> {

        public static final UnbakedPipeModel.Loader INSTANCE = new UnbakedPipeModel.Loader();

        private Loader() {}

        @Override
        public UnbakedPipeModel read(JsonObject jsonObject,
                                     JsonDeserializationContext deserializationContext) throws JsonParseException {
            if (!jsonObject.has("model_id"))
                throw new JsonParseException("An UnbakedPipeModel must have an \"model_id\" member.");

            String[] id = GsonHelper.getAsString(jsonObject, "model_id").split("#");
            ResourceLocation modelId = new ModelResourceLocation(new ResourceLocation(id[0]),
                    id.length > 1 ? id[1] : "");
            return new UnbakedPipeModel(AbstractPipeModel.MODELS.get(modelId));
        }
    }
}
