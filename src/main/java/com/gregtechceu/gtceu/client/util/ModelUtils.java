package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.client.util.Quad;

import java.util.List;
import java.util.function.Function;

public class ModelUtils {

    private ModelUtils() {}

    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos,
                                                     BlockState state, Direction side, RandomSource rand) {
        return model.getQuads(state, side, rand, model.getModelData(level, pos, state, ModelData.EMPTY), null);
    }

    public static BakedQuad offsetQuad(BakedQuad baked, float by) {
        Quad quad = Quad.from(baked);
        Direction quadOrientation = baked.getDirection();

        float xOffset = by * quadOrientation.getStepX();
        float yOffset = by * quadOrientation.getStepY();
        float zOffset = by * quadOrientation.getStepZ();
        for (int v = 0; v < 4; v++) {
            quad = quad.withVert(v, quad.getVert(v).add(xOffset, yOffset, zOffset));
        }
        return quad.rebake();
    }

    public static ModelManager getModelManager() {
        return Minecraft.getInstance().getModelManager();
    }

    public static ModelBakery getModelBakery() {
        return getModelManager().getModelBakery();
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

}
