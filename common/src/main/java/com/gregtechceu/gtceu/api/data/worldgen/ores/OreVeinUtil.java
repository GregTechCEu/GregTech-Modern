package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OreVeinUtil {
    private OreVeinUtil() {
    }


    public static boolean canPlaceOre(BlockState pState, Function<BlockPos, BlockState> pAdjacentStateAccessor,
                                      RandomSource pRandom, GTOreDefinition entry, OreConfiguration.TargetBlockState pTargetState,
                                      BlockPos.MutableBlockPos pMatablePos) {
        if (!pTargetState.target.test(pState, pRandom))
            return false;
        if (shouldSkipAirCheck(pRandom, entry.getDiscardChanceOnAirExposure()))
            return true;

        return !Feature.isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
    }

    public static boolean canPlaceOre(BlockState pState, Function<BlockPos, BlockState> pAdjacentStateAccessor,
                                      RandomSource pRandom, GTOreDefinition entry,
                                      BlockPos.MutableBlockPos pMatablePos) {
        if (!entry.getLayer().getTarget().test(pState, pRandom))
            return false;
        if (shouldSkipAirCheck(pRandom, entry.getDiscardChanceOnAirExposure()))
            return true;

        return !Feature.isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
    }

    protected static boolean shouldSkipAirCheck(RandomSource pRandom, float pChance) {
        return pChance <= 0 || (!(pChance >= 1) && pRandom.nextFloat() >= pChance);
    }
}
