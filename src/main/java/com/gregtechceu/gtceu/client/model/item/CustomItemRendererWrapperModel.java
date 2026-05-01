package com.gregtechceu.gtceu.client.model.item;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.geometry.*;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Custom model type that simply overrides {@link BakedModel#isCustomRenderer()} to always return true so
 * {@link IClientItemExtensions#getCustomRenderer()} can work. Does nothing on blocks.
 *
 * <p>
 * Use it on an item model builder like this:
 * <pre>{@code
 *      prov.getBuilder(ctx.getId())
 *              .customLoader(CustomItemRendererModel.Builder::begin).end()
 *              // ...
 * }</pre>
 */
public class CustomItemRendererWrapperModel implements IUnbakedGeometry<CustomItemRendererWrapperModel> {

    public static final ResourceLocation TYPE_ID = GTCEu.id("custom_item_renderer_wrapper");

    private final BlockModel parent;

    public CustomItemRendererWrapperModel(BlockModel parent) {
        this.parent = parent;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        BlockModel owner = parent;
        if (context instanceof BlockGeometryBakingContext blockContext) owner = blockContext.owner;

        BakedModel originalModel = parent.bake(baker, owner, spriteGetter, modelState, modelLocation, context.isGui3d());
        return new Baked(originalModel);
    }

    public static final class Baked extends BakedModelWrapper<BakedModel> {

        public Baked(BakedModel originalModel) {
            super(originalModel);
        }

        @Override
        public boolean isCustomRenderer() {
            return true;
        }
    }

    public static final class Loader implements IGeometryLoader<CustomItemRendererWrapperModel> {

        public static final CustomItemRendererWrapperModel.Loader INSTANCE = new CustomItemRendererWrapperModel.Loader();
//         private static final ThreadLocal<Boolean> currentlyParsing = new ThreadLocal<>();

        private Loader() {}

        @Override
        public @Nullable CustomItemRendererWrapperModel read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
//             if (Boolean.TRUE.equals(currentlyParsing.get())) {
//                 // if this is called from the inner context.deserialize(...) invocation, return null so the vanilla
//                 // model is parsed instead
//                 return null;
//             }
//             try {
//                 currentlyParsing.set(Boolean.TRUE);
//
            // remove the loader field and parse it again as a normal vanilla model
            json.remove("loader");

            return new CustomItemRendererWrapperModel(context.deserialize(json, BlockModel.class));
//             } finally {
//                 currentlyParsing.remove();
//             }
        }
    }

    /**
     * Use this to add the model type to an item model generator, like this:
     * <pre>{@code
     *      prov.withExistingParent(ctx::get, ctx.id())
     *              .customLoader(CustomItemRendererModel.Builder::begin).end()
     *              // ...
     * }</pre>
     */
    public static final class Builder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

        public Builder(T parent, ExistingFileHelper existingFileHelper) {
            super(TYPE_ID, parent, existingFileHelper);
        }

        public static <T extends ModelBuilder<T>> Builder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
            return new Builder<>(parent, existingFileHelper);
        }

    }
}
