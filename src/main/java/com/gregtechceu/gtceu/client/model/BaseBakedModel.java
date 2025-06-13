package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IDynamicBakedModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.client.util.Quad;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseBakedModel implements IDynamicBakedModel {

    public static final Set<BaseBakedModel> LISTENERS = new HashSet<>();

    @OnlyIn(Dist.CLIENT)
    protected Map<ModelState, BakedModel> bakedModelCache;

    public void onAdditionalModel(Consumer<ResourceLocation> consumer) {}

    public void registerEvent() {
        LISTENERS.add(this);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(MissingTextureAtlasSprite.getLocation());
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
                return BaseBakedModel::spriteGetter;
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
