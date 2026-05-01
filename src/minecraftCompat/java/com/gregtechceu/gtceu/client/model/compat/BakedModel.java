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
import net.minecraft.util.TriState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public interface BakedModel extends DynamicBlockStateModel, net.minecraft.client.renderer.item.ItemModel {

    /**
     * Pluggable per-tint-layer color resolver. Returns the ARGB value to push
     * into a {@link ItemStackRenderState.LayerRenderState} for {@code tintIndex}
     * on {@code stack}, or {@code -1} if the layer should not be tinted.
     * Default is a no-op so the compat layer has no reverse dependency on
     * gtceu's tint registry; gtceu installs its
     * {@code GTItemColors::getColor} implementation at client init.
     */
    AtomicReference<TintResolver> TINT_RESOLVER = new AtomicReference<>((stack, tintIndex) -> -1);

    @FunctionalInterface
    interface TintResolver {

        int getColor(ItemStack stack, int tintIndex);
    }

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
        for (BakedQuad quad : getAllQuads(null, 42L, ModelData.EMPTY, null)) {
            flags |= quad.materialInfo().flags();
        }
        return flags;
    }

    @Override
    default void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
                        ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner,
                        int seed) {
        renderState.appendModelIdentityElement(this);
        List<BakedQuad> quads = getAllQuads(null, seed, ModelData.EMPTY, null);
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        if (stack.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            renderState.setAnimated();
            renderState.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
        }
        applyItemTints(renderState, layer, stack, quads);
        layer.prepareQuadList().addAll(quads);
        layer.setUsesBlockLight(usesBlockLight());
        layer.setParticleMaterial(new Material.Baked(getParticleIcon(), false));
        layer.setItemTransform(getTransforms().unwrap().getTransform(displayContext));
        layer.setExtents(() -> CuboidItemModelWrapper.computeExtents(quads));
        if (hasMaterialFlag(quads, 2)) {
            renderState.setAnimated();
        }
    }

    private List<BakedQuad> getAllQuads(@Nullable BlockState state, long seed, ModelData modelData,
                                        @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>(getQuads(state, null, RandomSource.create(seed), modelData,
                renderType));
        for (Direction direction : Direction.values()) {
            quads.addAll(getQuads(state, direction, RandomSource.create(seed), modelData, renderType));
        }
        return quads;
    }

    private static void applyItemTints(ItemStackRenderState renderState, ItemStackRenderState.LayerRenderState layer,
                                       ItemStack stack, List<BakedQuad> quads) {
        int maxTintIndex = -1;
        for (BakedQuad quad : quads) {
            var material = quad.materialInfo();
            if (material.isTinted()) {
                maxTintIndex = Math.max(maxTintIndex, material.tintIndex());
            }
        }
        if (maxTintIndex < 0) {
            return;
        }

        IntList tintLayers = layer.tintLayers();
        TintResolver resolver = TINT_RESOLVER.get();
        for (int tintIndex = 0; tintIndex <= maxTintIndex; tintIndex++) {
            int tint = resolver.getColor(stack, tintIndex);
            tintLayers.add(tint);
            renderState.appendModelIdentityElement(tint);
        }
    }

    private static boolean hasMaterialFlag(List<BakedQuad> quads, int flag) {
        for (BakedQuad quad : quads) {
            if ((quad.materialInfo().flags() & flag) != 0) {
                return true;
            }
        }
        return false;
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
            for (BakedQuad quad : model.getAllQuads(state, seed, modelData, renderType)) {
                flags |= quad.materialInfo().flags();
            }
            return flags;
        }
    }
}
