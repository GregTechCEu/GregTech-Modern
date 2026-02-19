package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.GTModelProperties;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.common.util.strategy.IdentityStrategy;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class MultiPartBakedModel implements IDynamicBakedModel {

    private static final ModelProperty<Map<BakedModel, ModelData>> MULTI_PART_DATA_PROPERTY = new ModelProperty<>();

    private final List<Pair<Predicate<MachineRenderState>, BakedModel>> selectors;
    protected final boolean hasAmbientOcclusion;
    @Getter
    protected final boolean isGui3d;
    @Accessors(fluent = true)
    @Getter
    protected final boolean usesBlockLight;
    @Getter
    protected final TextureAtlasSprite particleIcon;
    @Getter
    protected final ItemTransforms transforms;
    @Getter
    protected final ItemOverrides overrides;
    private final Map<MachineRenderState, BitSet> selectorCache = new Object2ObjectOpenCustomHashMap<>(
            IdentityStrategy.IDENTITY);
    private final BakedModel defaultModel;

    @SuppressWarnings("deprecation")
    public MultiPartBakedModel(List<Pair<Predicate<MachineRenderState>, BakedModel>> selectors) {
        this.selectors = selectors;
        BakedModel defaultModel = selectors.getFirst().getRight();
        this.defaultModel = defaultModel;
        this.hasAmbientOcclusion = defaultModel.useAmbientOcclusion();
        this.isGui3d = defaultModel.isGui3d();
        this.usesBlockLight = defaultModel.usesBlockLight();
        this.particleIcon = defaultModel.getParticleIcon();
        this.transforms = defaultModel.getTransforms();
        this.overrides = defaultModel.getOverrides();
    }

    public BitSet getSelectors(@Nullable MachineRenderState state) {
        BitSet bitset = this.selectorCache.get(state);
        if (bitset == null) {
            bitset = new BitSet();

            for (int i = 0; i < this.selectors.size(); ++i) {
                Pair<Predicate<MachineRenderState>, BakedModel> pair = this.selectors.get(i);
                if (pair.getLeft().test(state)) {
                    bitset.set(i);
                }
            }

            this.selectorCache.put(state, bitset);
        }
        return bitset;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        return defaultModel.getQuads(state, side, rand, modelData, renderType);
    }

    public List<BakedQuad> getMachineQuads(MachineDefinition definition, MachineRenderState renderState,
                                           @Nullable BlockState blockState, @Nullable Direction direction,
                                           RandomSource random, ModelData modelData, @Nullable RenderType renderType) {
        if (blockState == null) blockState = definition.defaultBlockState();

        BitSet bitset = getSelectors(renderState);
        List<BakedQuad> quads = new LinkedList<>();
        long k = random.nextLong();

        for (int j = 0; j < bitset.length(); ++j) {
            if (bitset.get(j)) {
                var model = this.selectors.get(j).getRight();

                ModelData partData = resolveMultipartData(modelData, model);
                if (renderType == null || model.getRenderTypes(blockState, random, partData).contains(renderType)) {
                    quads.addAll(model.getQuads(blockState, direction, RandomSource.create(k), partData, renderType));
                }
            }
        }

        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData modelData) {
        BlockAndTintGetter level = modelData.get(GTModelProperties.LEVEL);
        BlockPos pos = modelData.get(GTModelProperties.POS);

        var machine = (level == null || pos == null) ? null : MetaMachine.getMachine(level, pos);
        // When machine is null (BE not loaded yet), use the default model's render types
        // to ensure we still render something instead of being invisible
        if (machine == null) return defaultModel.getRenderTypes(state, rand, modelData);

        var renderTypeSets = new LinkedList<ChunkRenderTypeSet>();
        var selectors = getSelectors(machine.getRenderState());
        for (int i = 0; i < selectors.length(); i++) {
            if (selectors.get(i)) {
                BakedModel model = this.selectors.get(i).getRight();

                ModelData partData = resolveMultipartData(modelData, model);
                renderTypeSets.add(model.getRenderTypes(state, rand, partData));
            }
        }
        return ChunkRenderTypeSet.union(renderTypeSets);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ModelData.Builder builder = modelData.derive()
                .with(GTModelProperties.LEVEL, level)
                .with(GTModelProperties.POS, pos);

        var machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return builder.build();

        addMachineModelData(machine.getRenderState(), level, pos, state, modelData, builder);
        return builder.build();
    }

    public void addMachineModelData(MachineRenderState renderState, BlockAndTintGetter level, BlockPos pos,
                                    BlockState state, ModelData baseData, ModelData.Builder builder) {
        // Don't allocate memory if no submodel changes the model data
        Map<BakedModel, ModelData> dataMap = null;

        BitSet selected = getSelectors(renderState);

        for (int i = 0; i < selected.length(); ++i) {
            if (selected.get(i)) {
                var model = selectors.get(i).getRight();
                var data = model.getModelData(level, pos, state, baseData);

                if (data != baseData) {
                    if (dataMap == null)
                        dataMap = new IdentityHashMap<>();

                    dataMap.put(model, data);
                }
            }
        }
        if (dataMap != null) {
            builder.with(MULTI_PART_DATA_PROPERTY, dataMap);
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.hasAmbientOcclusion;
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return this.defaultModel.useAmbientOcclusion(state, data, renderType);
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData modelData) {
        BlockAndTintGetter level = modelData.get(GTModelProperties.LEVEL);
        BlockPos pos = modelData.get(GTModelProperties.POS);

        var machine = (level == null || pos == null) ? null : MetaMachine.getMachine(level, pos);
        if (machine != null) return getParticleIcon(machine.getRenderState(), modelData);
        else return this.defaultModel.getParticleIcon(modelData);
    }

    public TextureAtlasSprite getParticleIcon(@NotNull MachineRenderState renderState, ModelData modelData) {
        var selectors = getSelectors(renderState);
        for (int i = 0; i < selectors.length(); i++) {
            if (selectors.get(i)) {
                BakedModel model = this.selectors.get(i).getRight();
                ModelData partData = resolveMultipartData(modelData, model);

                return model.getParticleIcon(partData);
            }
        }
        return this.defaultModel.getParticleIcon(modelData);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
                                     boolean applyLeftHandTransform) {
        return this.defaultModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    public static ModelData resolveMultipartData(ModelData modelData, BakedModel model) {
        var multipartData = modelData.get(MULTI_PART_DATA_PROPERTY);
        if (multipartData == null) {
            return modelData;
        }
        var partData = multipartData.get(model);
        return partData != null ? partData : modelData;
    }

    public static class Builder {

        private final List<Pair<Predicate<MachineRenderState>, BakedModel>> selectors = new ArrayList<>();

        public void add(Predicate<MachineRenderState> predicate, BakedModel model) {
            this.selectors.add(Pair.of(predicate, model));
        }

        public MultiPartBakedModel build() {
            return new MultiPartBakedModel(this.selectors);
        }
    }
}
