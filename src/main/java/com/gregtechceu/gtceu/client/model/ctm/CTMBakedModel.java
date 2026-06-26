package com.gregtechceu.gtceu.client.model.ctm;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.client.model.GTModelProperties.*;

public class CTMBakedModel<T extends BakedModel> extends BakedModelWrapper<T> {

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
        ModelData parentModelData = data.has(PARENT_MODEL_DATA) ? data.get(PARENT_MODEL_DATA) : data;
        if (state == null || side == null) {
            return super.getQuads(state, side, rand, parentModelData, renderType);
        }
        BlockAndTintGetter level = data.get(LEVEL);
        BlockPos pos = data.get(POS);
        if (level == null || pos == null) {
            return super.getQuads(state, side, rand, parentModelData, renderType);
        }

        TextureConnections connections = TextureConnections.getInstance();
        connections.fillSubmapCache(level, pos, state, side);
        return CTMMeshBuilder.buildCTMQuads(connections, super.getQuads(state, side, rand, parentModelData, renderType),
                side);
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
}
