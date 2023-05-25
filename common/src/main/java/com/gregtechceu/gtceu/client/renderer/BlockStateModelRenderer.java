package com.gregtechceu.gtceu.client.renderer;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/3/24
 * @implNote BlockStateModelRenderer
 */
public class BlockStateModelRenderer implements IRenderer {
    private final Map<BlockState, IRenderer> models;

    public BlockStateModelRenderer(Block block, Function<BlockState, IRenderer> predicate) {
        this.models = new HashMap<>();
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            models.put(state, predicate.apply(state));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean useAO() {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side, RandomSource rand) {
        if (models.containsKey(state)) {
            return models.get(state).renderModel(level, pos, state, side, rand);
        }
        return Collections.emptyList();
    }

}
