package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.model.connected.CTMCache;
import com.gregtechceu.gtceu.client.util.QuadUtils;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.gregtechceu.gtceu.client.model.GTModelProperties.*;

public class CTMBakedModel<T extends BakedModel> extends BakedModelWrapper<T> {

    private final Map<Direction, Map<CTMCache, List<BakedQuad>>> sideCache = new EnumMap<>(Direction.class);

    public CTMBakedModel(T parent) {
        super(parent);
    }

    public BakedModel getParent() {
        return this.originalModel;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        BlockAndTintGetter level = data.get(LEVEL);
        BlockPos pos = data.get(POS);
        ModelData parentData = data.has(PARENT_MODEL_DATA) ? data.get(PARENT_MODEL_DATA) : data;

        if (level != null && pos != null && state != null) {
            return getCustomQuads(level, pos, state, side, rand, parentData, renderType);
        } else {
            return super.getQuads(state, side, rand, parentData, renderType);
        }
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ModelData parentModelData = super.getModelData(level, pos, state, modelData);
        return ModelData.builder()
                .with(LEVEL, level)
                .with(POS, pos)
                .with(PARENT_MODEL_DATA, parentModelData)
                .build();
    }

    public List<BakedQuad> getCustomQuads(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                          @Nullable Direction side, RandomSource rand,
                                          ModelData modelData, @Nullable RenderType renderType) {
        if (side == null) {
            return super.getQuads(state, null, rand, modelData, renderType);
        }

        CTMCache ctmCache = CTMCache.getInstance();
        ctmCache.getSubmapIds(level, pos, state, side);
        return this.sideCache.computeIfAbsent(side, $ -> new ConcurrentHashMap<>())
                .computeIfAbsent(ctmCache, cache -> QuadUtils.buildCTMQuads(cache,
                        super.getQuads(state, side, rand, modelData, renderType)));
    }
}
