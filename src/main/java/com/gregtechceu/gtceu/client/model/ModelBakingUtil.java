package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.compat.ChunkRenderTypeSet;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;
import com.gregtechceu.gtceu.client.model.compat.ModelState;
import com.gregtechceu.gtceu.client.model.machine.variant.MultiVariantModel;
import com.gregtechceu.gtceu.client.model.machine.variant.VariantState;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ModelBakingUtil {

    private ModelBakingUtil() {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static BakedModel bake(UnbakedModel unbakedModel, IGeometryBakingContext context, ModelBaker baker,
                                  Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                                  ItemOverrides overrides) {
        if (unbakedModel instanceof MultiVariantModel multiVariantModel) {
            return bakeMultiVariant(multiVariantModel, context, baker, spriteGetter, overrides);
        }
        if (unbakedModel instanceof IUnbakedGeometry geometry) {
            return geometry.bake(context, baker, spriteGetter, modelState, overrides);
        }
        var resolvedModel = baker.resolveInlineModel(unbakedModel, () -> unbakedModel.getClass().getName());
        return new PartBakedModel(SimpleModelWrapper.bake(baker, resolvedModel, modelState));
    }

    public static BakedModel bakeMultiVariant(MultiVariantModel multiVariantModel, IGeometryBakingContext context,
                                              ModelBaker baker,
                                              Function<Material, TextureAtlasSprite> spriteGetter,
                                              ItemOverrides overrides) {
        List<WeightedBakedModel.Entry> entries = new ArrayList<>();
        for (VariantState variant : multiVariantModel.variants()) {
            BakedModel bakedModel = variant.getModel()
                    .map(modelId -> new PartBakedModel(SimpleModelWrapper.bake(baker, modelId, variant)),
                            inlineModel -> bake(variant.getResolvedModel() == null ? inlineModel :
                                    variant.getResolvedModel(), context, baker, spriteGetter, variant, overrides));
            entries.add(new WeightedBakedModel.Entry(bakedModel, variant.getWeight()));
        }
        if (entries.isEmpty()) {
            return new PartBakedModel(baker.missingBlockModelPart());
        }
        if (entries.size() == 1) {
            return entries.getFirst().model();
        }
        return new WeightedBakedModel(entries);
    }

    private record PartBakedModel(BlockStateModelPart part) implements BakedModel {

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                        ModelData modelData, @Nullable RenderType renderType) {
            List<BakedQuad> quads = part.getQuads(side);
            if (renderType == null) {
                return quads;
            }
            return quads.stream()
                    .filter(quad -> quad.materialInfo().itemRenderType() == renderType ||
                            quad.materialInfo().layer().pipeline() == renderType.pipeline())
                    .toList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return part.useAmbientOcclusion();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return part.particleMaterial().sprite();
        }
    }

    private record WeightedBakedModel(List<Entry> entries, int totalWeight) implements BakedModel {

        private WeightedBakedModel(List<Entry> entries) {
            this(List.copyOf(entries), entries.stream().mapToInt(Entry::weight).sum());
        }

        private BakedModel select(RandomSource random) {
            int value = random.nextInt(totalWeight);
            for (Entry entry : entries) {
                value -= entry.weight();
                if (value < 0) {
                    return entry.model();
                }
            }
            return entries.getLast().model();
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                        ModelData modelData, @Nullable RenderType renderType) {
            return select(rand).getQuads(state, side, rand, modelData, renderType);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return entries.getFirst().model().useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return entries.getFirst().model().isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return entries.getFirst().model().usesBlockLight();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return entries.getFirst().model().getParticleIcon();
        }

        @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData modelData) {
            return ChunkRenderTypeSet.union(entries.stream()
                    .map(entry -> entry.model().getRenderTypes(state, rand, modelData))
                    .toList());
        }

        private record Entry(BakedModel model, int weight) {}
    }
}
