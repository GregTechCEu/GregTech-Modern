package com.gregtechceu.gtceu.client.renderer;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    @OnlyIn(Dist.CLIENT)
    public boolean useAO() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side,
                                       RandomSource rand) {
        if (models.containsKey(state)) {
            return models.get(state).renderModel(level, pos, state, side, rand);
        }
        return Collections.emptyList();
    }
}
