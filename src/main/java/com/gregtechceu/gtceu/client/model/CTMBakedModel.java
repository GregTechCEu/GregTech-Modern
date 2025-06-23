package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.QuadInfo;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Submap;
import com.lowdragmc.lowdraglib.client.model.custommodel.Connections;
import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CTMBakedModel<T extends BakedModel> extends BakedModelWrapper<T> {

    private final Map<Direction, Map<Connections, List<BakedQuad>>> sideCache;
    private final List<BakedQuad> noSideCache;

    public static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();
    public static final ModelProperty<ModelData> MODEL_DATA = new ModelProperty<>();

    public CTMBakedModel(T parent) {
        super(parent);
        this.sideCache = new EnumMap<>(Direction.class);
        this.noSideCache = new ArrayList<>();
    }

    public BakedModel getParent() {
        return this.originalModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData data,
                                             @Nullable RenderType renderType) {
        BlockAndTintGetter level = data.get(LEVEL);
        BlockPos pos = data.get(POS);
        if (level != null && pos != null && state != null) {
            return getCustomQuads(level, pos, state, side, rand, data, renderType);
        } else {
            return super.getQuads(state, side, rand, data, renderType);
        }
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ModelData modelData) {
        modelData = super.getModelData(level, pos, state, modelData);
        return ModelData.builder()
                .with(LEVEL, level)
                .with(POS, pos)
                .with(MODEL_DATA, modelData)
                .build();
    }

    @SuppressWarnings("DataFlowIssue")
    @NotNull
    public List<BakedQuad> getCustomQuads(BlockAndTintGetter level, BlockPos pos, @NotNull BlockState state,
                                          @Nullable Direction elementSide, RandomSource rand,
                                          @NotNull ModelData data, @Nullable RenderType renderType) {
        final ModelData parentData = data.has(MODEL_DATA) ? data.get(MODEL_DATA) : ModelData.EMPTY;

        var connections = Connections.checkConnections(level, pos, state, elementSide);
        if (elementSide == null) {
            if (noSideCache.isEmpty()) {
                noSideCache.addAll(buildCustomQuads(connections,
                        super.getQuads(state, null, rand, parentData, renderType)));
            }
            return noSideCache;
        }
        return sideCache.computeIfAbsent(elementSide, this::makeMap)
                .computeIfAbsent(connections, c -> buildCustomQuads(c,
                        super.getQuads(state, elementSide, rand, parentData, renderType)));
    }

    public static List<BakedQuad> reBakeCustomQuads(List<BakedQuad> quads, BlockAndTintGetter level, BlockPos pos,
                                                    @NotNull BlockState state, @Nullable Direction elementSide) {
        return buildCustomQuads(Connections.checkConnections(level, pos, state, elementSide), quads);
    }

    public static List<BakedQuad> buildCustomQuads(Connections connections, List<BakedQuad> base) {
        List<BakedQuad> result = new ArrayList<>();
        for (BakedQuad bakedQuad : base) {
            var section = LDLMetadataSection.getMetadata(bakedQuad.getSprite());
            TextureAtlasSprite connection = section.connection == null ? null :
                    ModelUtils.getBlockSprite(section.connection);
            if (connection == null) {
                result.add(makeQuad(bakedQuad, section));
                continue;
            }

            BakedQuad baked = ModelUtils.derotateQuad(makeQuad(bakedQuad, section));
            QuadInfo[] subdivided = ModelUtils.subdivide(baked);

            int[] ctm = connections.getSubmapIndices();

            for (int j = 0; j < subdivided.length; j++) {
                QuadInfo quad = subdivided[j];
                if (quad != null) {
                    int quadrant = quad.getNormalizedUVQuadrant();
                    TextureAtlasSprite ctmSprite = ctm[quadrant] > 15 ? bakedQuad.getSprite() : connection;
                    subdivided[j] = quad.grow().transformUVs(ctmSprite, Submap.uvs[ctm[quadrant]]);
                }
            }
            result.addAll(Arrays.stream(subdivided).filter(Objects::nonNull).map(QuadInfo::rebake).toList());
        }
        return result;
    }

    protected static BakedQuad makeQuad(BakedQuad quad, LDLMetadataSection section) {
        int light = section.emissive ? 15 : 0;
        QuadTransformers.settingEmissivity(light).process(quad);
        return quad;
    }

    private Map<Connections, List<BakedQuad>> makeMap(Direction ignored) {
        return new ConcurrentHashMap<>();
    }
}
