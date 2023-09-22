package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.common.data.GTOres;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
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

    /**
     * Resolves a vein's center for the supplied chunk position.
     * 
     * <p>Note that depending on the config value for the random vein offset, its actual
     * center may be outside the supplied chunk.
     * 
     * @return The origin of the vein to be generated.<br>
     *         {@code Optional.empty()} if no vein should exist for the specified chunk.
     */
    public static Optional<BlockPos> getVeinCenter(ChunkPos chunkPos, RandomSource random) {
        int gridSize = ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize;
        int randomOffset = ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinRandomOffset;

        if (chunkPos.x % gridSize != 0 || chunkPos.z % gridSize != 0)
            return Optional.empty();

        var chunkCenter = chunkPos.getMiddleBlockPosition(0);

        return Optional.of(chunkCenter.offset(
                random.nextInt(-randomOffset, +randomOffset),
                0,
                random.nextInt(-randomOffset, +randomOffset)
        ));
    }

    /**
     * @return The radius (in chunks) to search for adjacent veins.<br>
     *         Depends on the largest registered vein size, as well as the configured random vein offset.
     */
    static int getMaxVeinSearchDistance() {
        double halfVeinSize = GTOres.getLargestVeinSize() / 2.0;
        int randomOffset = ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinRandomOffset;

        return (int) Math.ceil((halfVeinSize + randomOffset) / 16.0);
    }
}
