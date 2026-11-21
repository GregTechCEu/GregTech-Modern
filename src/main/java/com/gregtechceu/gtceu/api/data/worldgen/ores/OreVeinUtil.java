package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTOres;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OreVeinUtil {

    private OreVeinUtil() {}

    public static void placeOreBlock(Either<List<OreConfiguration.TargetBlockState>, Material> blockOrMaterial,
                                     BulkSectionAccess access, LevelChunkSection section,
                                     RandomSource random, GTOreDefinition entry, BlockPos pos) {
        int sx = SectionPos.sectionRelative(pos.getX());
        int sy = SectionPos.sectionRelative(pos.getY());
        int sz = SectionPos.sectionRelative(pos.getZ());

        BlockState existing = section.getBlockState(sx, sy, sz);

        blockOrMaterial.ifLeft(targetStates -> {
            for (OreConfiguration.TargetBlockState target : targetStates) {
                if (!canPlaceOre(existing, access::getBlockState, random, entry, target, pos)) continue;
                if (target.state.isAir()) continue;

                section.setBlockState(sx, sy, sz, target.state, false);
                break;
            }
        }).ifRight(material -> {
            if (!canPlaceOre(existing, access::getBlockState, random, entry, pos)) return;
            Optional<TagPrefix> prefix = ChemicalHelper.getOrePrefix(existing);
            if (prefix.isEmpty()) return;

            Block toPlace = ChemicalHelper.getBlock(prefix.get(), material);
            if (toPlace == null || toPlace.defaultBlockState().isAir()) return;

            section.setBlockState(sx, sy, sz, toPlace.defaultBlockState(), false);
        });
    }

    public static boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> adjacentStateAccessor,
                                      RandomSource random, GTOreDefinition entry,
                                      OreConfiguration.TargetBlockState targetState, BlockPos pos) {
        if (!targetState.target.test(state, random)) return false;
        if (shouldSkipAirCheck(random, entry.discardChanceOnAirExposure())) return true;

        return !Feature.isAdjacentToAir(adjacentStateAccessor, pos);
    }

    public static boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> adjacentStateAccessor,
                                      RandomSource random, GTOreDefinition entry, BlockPos pos) {
        if (!entry.layer().getTarget().test(state, random)) return false;
        if (shouldSkipAirCheck(random, entry.discardChanceOnAirExposure())) return true;

        return !Feature.isAdjacentToAir(adjacentStateAccessor, pos);
    }

    protected static boolean shouldSkipAirCheck(RandomSource random, float chance) {
        if (chance <= 0.0f) return true;
        if (chance >= 1.0f) return false;
        return random.nextFloat() >= chance;
    }

    /**
     * Resolves a vein's center for the supplied chunk position.
     * 
     * <p>
     * Note that depending on the config value for the random vein offset, its actual
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

        if (randomOffset == 0)
            return Optional.of(chunkCenter);

        return Optional.of(chunkCenter.offset(
                random.nextInt(-randomOffset, +randomOffset),
                0,
                random.nextInt(-randomOffset, +randomOffset)));
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

    /**
     * @return The radius (in chunks) to search for adjacent indicators.<br>
     *         Depends on the largest registered indicator size, as well as the configured random vein offset.
     */
    static int getMaxIndicatorSearchDistance() {
        return getMaxVeinSearchDistance() + (int) Math.ceil((double) GTOres.getLargestIndicatorOffset() / 16.0);
    }

    @Nullable
    public static Supplier<HolderSet<Biome>> resolveBiomes(List<String> biomes) {
        if (biomes.isEmpty())
            return null;

        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        JsonElement codecInput = resolveBiomeCodecInput(biomes);
        return Suppliers.memoize(() -> RegistryCodecs.homogeneousList(Registries.BIOME)
                .parse(registryOps, codecInput)
                .getOrThrow(false, GTCEu.LOGGER::error));
    }

    private static JsonElement resolveBiomeCodecInput(List<String> biomes) {
        if (biomes.size() == 1)
            return new JsonPrimitive(biomes.get(0));

        if (biomes.stream().anyMatch(filter -> filter.startsWith("#")))
            throw new IllegalStateException(
                    "Cannot resolve biomes: You may use either a single tag or multiple individual biomes.");

        var jsonArray = new JsonArray();
        biomes.forEach(jsonArray::add);
        return jsonArray;
    }
}
