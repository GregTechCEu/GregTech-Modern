package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ModelEvent.ModifyBakingResult;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelUtils {

    private ModelUtils() {}

    private static final Set<Consumer<ModifyBakingResult>> BAKE_EVENT_LISTENERS = new ReferenceOpenHashSet<>();

    private static final Function<Float, IQuadTransformer> OFFSET_BY = Util.memoize(by -> {
        if (by == 0.0f) return QuadTransformers.empty();

        return quad -> {
            var vertices = quad.getVertices();
            Direction direction = quad.getDirection();

            for (int i = 0; i < 4; i++) {
                int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
                float x = Float.intBitsToFloat(vertices[offset]);
                float y = Float.intBitsToFloat(vertices[offset + 1]);
                float z = Float.intBitsToFloat(vertices[offset + 2]);
                x += by * direction.getStepX();
                y += by * direction.getStepY();
                z += by * direction.getStepZ();

                vertices[offset] = Float.floatToRawIntBits(x);
                vertices[offset + 1] = Float.floatToRawIntBits(y);
                vertices[offset + 2] = Float.floatToRawIntBits(z);
            }
        };
    });

    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos,
                                                     BlockState state, Direction side, RandomSource rand) {
        return model.getQuads(state, side, rand, model.getModelData(level, pos, state, ModelData.EMPTY), null);
    }

    public static BakedQuad offsetQuad(BakedQuad quad, float by) {
        return OFFSET_BY.apply(by).process(quad);
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

    public static TextureAtlasSprite spriteGetter(Material material) {
        return material.sprite();
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
                return ModelUtils::spriteGetter;
            }

            @Override
            public @NotNull UnbakedModel getModel(@NotNull ResourceLocation location) {
                return getModelBakery().getModel(location);
            }

            @Override
            public BakedModel bake(@NotNull ResourceLocation location, @NotNull ModelState transform) {
                return this.bake(location, transform, getModelTextureGetter());
            }
        };
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

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (var consumer : BAKE_EVENT_LISTENERS) {
            consumer.accept(event);
        }
    }

}
