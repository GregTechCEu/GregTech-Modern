package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelUtils {

    private ModelUtils() {}

    private static final Set<Consumer<ModifyBakingResult>> BAKE_EVENT_LISTENERS = new ReferenceOpenHashSet<>();
    private static final Set<Consumer<RegisterAdditional>> ADD_MODELS_EVENT_LISTENERS = new ReferenceOpenHashSet<>();

    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos,
                                                     BlockState state, Direction side, RandomSource rand) {
        return model.getQuads(state, side, rand, model.getModelData(level, pos, state, ModelData.EMPTY), null);
    }

    public static Vector2f[] getQuadUVs(int[] vertices) {
        Vector2f[] uvs = new Vector2f[4];

        for (int i = 0; i < 4; i++) {
            int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
            float u = Float.intBitsToFloat(vertices[offset]);
            float v = Float.intBitsToFloat(vertices[offset + 1]);
            uvs[i] = new Vector2f(u, v);
        }
        return uvs;
    }

    public static Vector3f[] getQuadVertices(int[] vertices) {
        Vector3f[] vertPos = new Vector3f[4];

        for (int i = 0; i < 4; i++) {
            int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
            float x = Float.intBitsToFloat(vertices[offset]);
            float y = Float.intBitsToFloat(vertices[offset + 1]);
            float z = Float.intBitsToFloat(vertices[offset + 2]);
            vertPos[i] = new Vector3f(x, y, z);
        }
        return vertPos;
    }

    public static QuadInfo[] subdivide(BakedQuad baked) {
        Vector3f[] vertPos = getQuadVertices(baked.getVertices());
        Vector2f[] uvs = getQuadUVs(baked.getVertices());
        var maxUVs = findMinMaxUVs(uvs);
        QuadInfo quad = new QuadInfo(baked.getSprite(), baked.getTintIndex(), baked.getDirection(),
                baked.isShade(), baked.hasAmbientOcclusion(),
                vertPos, uvs, maxUVs.first(), maxUVs.second());

        return quad.subdivide();
    }

    private static void putVertexData(int[] vertices, int index, Vector3f pos, Vector2f uv) {
        int posOffset = index * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
        vertices[posOffset] = Float.floatToRawIntBits(pos.x());
        vertices[posOffset + 1] = Float.floatToRawIntBits(pos.y());
        vertices[posOffset + 2] = Float.floatToRawIntBits(pos.z());

        int uvOffset = index * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
        vertices[uvOffset] = Float.floatToRawIntBits(uv.x());
        vertices[uvOffset + 1] = Float.floatToRawIntBits(uv.y());
    }

    public static Vector2f[] normalizeUVs(Vector2f min, Vector2f max, Vector2f... uvs) {
        Vector2f[] ret = new Vector2f[uvs.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ModelUtils.normalizeUV(min, max, uvs[i]);
        }
        return ret;
    }

    public static Vector2f normalizeUV(TextureAtlasSprite sprite, Vector2f vec) {
        return normalizeUV(
                new Vector2f(sprite.getU0(), sprite.getU1()),
                new Vector2f(sprite.getV0(), sprite.getV1()),
                vec);
    }

    public static Vector2f normalizeUV(Vector2f min, Vector2f max, Vector2f vec) {
        return new Vector2f(GTMath.normalize(min.x(), max.x(), vec.x()),
                GTMath.normalize(min.y(), max.y(), vec.y()));
    }

    public static Vector2f[] relativizeUVs(TextureAtlasSprite sprite, Vector2f... uvs) {
        for (int i = 0; i < uvs.length; i++) {
            uvs[i] = relativizeUV(sprite, uvs[i]);
        }
        return uvs;
    }

    public static Vector2f relativizeUV(TextureAtlasSprite sprite, Vector2f vec) {
        return new Vector2f(
                Mth.lerp(vec.x(), sprite.getU0(), sprite.getU1()),
                Mth.lerp(vec.y(), sprite.getV0(), sprite.getV1()));
    }

    public static Pair<Vector2f, Vector2f> findMinMaxUVs(Vector2f[] uvs) {
        float minU = Float.MAX_VALUE, minV = Float.MAX_VALUE, maxU = Float.MIN_VALUE, maxV = Float.MIN_VALUE;

        for (int i = 0; i < 4; i++) {
            Vector2f uv = uvs[i];
            minU = Math.min(minU, uv.x());
            minV = Math.min(minV, uv.y());
            maxU = Math.max(maxU, uv.x());
            maxV = Math.max(maxV, uv.y());
        }
        return Pair.of(new Vector2f(minU, minV), new Vector2f(maxU, maxV));
    }

    public static ModelManager getModelManager() {
        return Minecraft.getInstance().getModelManager();
    }

    public static ModelBakery getModelBakery() {
        return getModelManager().getModelBakery();
    }

    public static TextureAtlasSprite getSprite(ResourceLocation atlas, ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(atlas).apply(texture);
    }

    @SuppressWarnings("deprecation")
    public static TextureAtlasSprite getBlockSprite(@NotNull ResourceLocation texture) {
        return getSprite(TextureAtlas.LOCATION_BLOCKS, texture);
    }

    public static ModelBaker getModelBaker() {
        return new ModelBaker() {

            @Override
            public @Nullable BakedModel bake(@NotNull ResourceLocation location, @NotNull ModelState transform,
                                             @NotNull Function<Material, TextureAtlasSprite> sprites) {
                UnbakedModel unbakedmodel = this.getModel(location);
                if (unbakedmodel instanceof BlockModel blockModel) {
                    if (blockModel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                        return ModelBakery.ITEM_MODEL_GENERATOR.generateBlockModel(Material::sprite, blockModel)
                                .bake(this, blockModel, sprites, transform, location, false);
                    }
                }
                return unbakedmodel.bake(this, sprites, transform, location);
            }

            @Override
            public @NotNull Function<Material, TextureAtlasSprite> getModelTextureGetter() {
                return Material::sprite;
            }
        }
    }

    public static ModelState getModelStateFromDirection(Direction facing) {
        return switch (facing) {
            case DOWN -> BlockModelRotation.X90_Y0;
            case UP -> BlockModelRotation.X270_Y0;
            case NORTH -> BlockModelRotation.X0_Y0;
            case SOUTH -> BlockModelRotation.X0_Y180;
            case WEST -> BlockModelRotation.X0_Y270;
            case EAST -> BlockModelRotation.X0_Y90;
        };
    }

    public static void registerBakeEventListener(Consumer<ModifyBakingResult> consumer) {
        BAKE_EVENT_LISTENERS.add(consumer);
    }

    public static void registerAddModelsEventListener(Consumer<RegisterAdditional> consumer) {
        ADD_MODELS_EVENT_LISTENERS.add(consumer);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (var consumer : BAKE_EVENT_LISTENERS) {
            consumer.accept(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (var consumer : ADD_MODELS_EVENT_LISTENERS) {
            consumer.accept(event);
        }
    }
}
