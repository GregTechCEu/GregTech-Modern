package com.gregtechceu.gtceu.client.model.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface BakedModel extends DynamicBlockStateModel, net.minecraft.client.renderer.item.ItemModel {

    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                     ModelData modelData, @Nullable RenderType renderType) {
        return List.of();
    }

    default boolean useAmbientOcclusion() {
        return true;
    }

    default TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return useAmbientOcclusion() ? TriState.TRUE : TriState.FALSE;
    }

    default boolean isGui3d() {
        return true;
    }

    default boolean usesBlockLight() {
        return true;
    }

    default boolean isCustomRenderer() {
        return false;
    }

    default TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(MissingTextureAtlasSprite.getLocation());
    }

    default TextureAtlasSprite getParticleIcon(ModelData modelData) {
        return getParticleIcon();
    }

    default ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    default ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData modelData) {
        return ChunkRenderTypeSet.all();
    }

    default List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return List.of(this);
    }

    default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return modelData;
    }

    default BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
                                      boolean applyLeftHandTransform) {
        getTransforms().getTransform(transformType).apply(applyLeftHandTransform, poseStack.last());
        return this;
    }

    @Override
    default void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                              List<BlockStateModelPart> parts) {
        ModelData modelData = getModelData(level, pos, state, level.getModelData(pos));
        parts.add(new CompatPart(this, state, random.nextLong(), modelData, null));
    }

    @Override
    default void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(new CompatPart(this, null, random.nextLong(), ModelData.EMPTY, null));
    }

    @Override
    default Material.Baked particleMaterial() {
        return new Material.Baked(getParticleIcon(), false);
    }

    @Override
    default int materialFlags() {
        int flags = 0;
        for (BakedQuad quad : getQuads(null, null, RandomSource.create(42L), ModelData.EMPTY, null)) {
            flags |= quad.materialInfo().flags();
        }
        return flags;
    }

    @Override
    default void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
                        ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner,
                        int seed) {
        List<BakedQuad> quads = new ArrayList<>(getQuads(null, null, RandomSource.create(seed), ModelData.EMPTY, null));
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        layer.prepareQuadList().addAll(quads);
        layer.setUsesBlockLight(usesBlockLight());
        layer.setParticleMaterial(new Material.Baked(getParticleIcon(), false));
        layer.setItemTransform(getTransforms().unwrap().getTransform(displayContext));
        layer.setExtents(() -> CuboidItemModelWrapper.computeExtents(quads));
    }

    record CompatPart(BakedModel model, @Nullable BlockState state, long seed, ModelData modelData,
                      @Nullable RenderType renderType)
            implements BlockStateModelPart {

        @Override
        public List<BakedQuad> getQuads(Direction direction) {
            return model.getQuads(state, direction, RandomSource.create(seed), modelData, renderType);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return model.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return new Material.Baked(model.getParticleIcon(modelData), false);
        }

        @Override
        public int materialFlags() {
            int flags = 0;
            for (BakedQuad quad : model.getQuads(state, null, RandomSource.create(seed), modelData, renderType)) {
                flags |= quad.materialInfo().flags();
            }
            return flags;
        }
    }
}
