package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreVeinSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

/**
 * @author Screret
 * @date 2023/6/9
 * @implNote GTOreFeature
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTOreFeature extends Feature<GTOreFeatureConfiguration> {

    public GTOreFeature() {
        super(GTOreFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<GTOreFeatureConfiguration> context) {
        RandomSource random = context.random();
        BlockPos origin = new ChunkPos(context.origin()).getMiddleBlockPosition(context.origin().getY());
        WorldGenLevel level = context.level();
        Holder<Biome> biome = context.level().getBiome(origin);

        GTOreDefinition entry = context.config().getEntry(context.level(), biome, random);
        if (entry == null) return false;
        context.config().setEntry(null);
        /*HolderSet<Biome> checkingBiomes = entry.datagenExt().biomes.map(left -> left, right -> BuiltinRegistries.BIOME.getTag(right).orElse(BuiltinRegistries.BIOME.getTag(BiomeTags.IS_OVERWORLD).orElseThrow()));

        while (!checkingBiomes.contains(biome)) {
            entry = context.config().getEntry(context.level(), biome, random);
            if (entry == null) return false;
            context.config().setEntry(null);
            checkingBiomes = entry.datagenExt().biomes.map(left -> left, right -> BuiltinRegistries.BIOME.getTag(right).orElse(BuiltinRegistries.BIOME.getTag(BiomeTags.IS_OVERWORLD).orElseThrow()));
            if (ConfigHolder.INSTANCE.worldgen.debugWorldgen) GTCEu.LOGGER.debug("failed to place vein " + entry.id + " in biome " + biome + ". Trying with another...");
        }*/

        ResourceLocation id = GTRegistries.ORE_VEINS.getKey(entry);
        if (ConfigHolder.INSTANCE.worldgen.debugWorldgen) GTCEu.LOGGER.debug("trying to place vein " + id + " at " + origin);
        if (entry.getVeinGenerator() != null && entry.getVeinGenerator().generate(level, random, entry, origin)) {
            logPlaced(id, true);
            if (ConfigHolder.INSTANCE.machines.doBedrockOres) {
                BedrockOreVeinSavedData.getOrCreate(level.getLevel()).createVein(new ChunkPos(origin), entry);
            }
            return true;
        }

        logPlaced(id, false);
        return false;
    }

    public static boolean canPlaceOre(BlockState pState, Function<BlockPos, BlockState> pAdjacentStateAccessor,
                                      RandomSource pRandom, GTOreDefinition entry, OreConfiguration.TargetBlockState pTargetState,
                                      BlockPos.MutableBlockPos pMatablePos) {
        if (!pTargetState.target.test(pState, pRandom))
            return false;
        if (shouldSkipAirCheck(pRandom, entry.getDiscardChanceOnAirExposure()))
            return true;

        return !isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
    }

    public static boolean canPlaceOre(BlockState pState, Function<BlockPos, BlockState> pAdjacentStateAccessor,
                                      RandomSource pRandom, GTOreDefinition entry,
                                      BlockPos.MutableBlockPos pMatablePos) {
        if (!entry.getLayer().getTarget().test(pState, pRandom))
            return false;
        if (shouldSkipAirCheck(pRandom, entry.getDiscardChanceOnAirExposure()))
            return true;

        return !isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
    }

    protected static boolean shouldSkipAirCheck(RandomSource pRandom, float pChance) {
        return pChance <= 0 || (!(pChance >= 1) && pRandom.nextFloat() >= pChance);
    }

    public void logPlaced(ResourceLocation entry, boolean didPlace) {
        if (ConfigHolder.INSTANCE.worldgen.debugWorldgen) GTCEu.LOGGER.debug("Did place vein " + entry + ": " + didPlace);
    }
}
