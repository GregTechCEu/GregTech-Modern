package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.GTCEu.LOGGER;

public abstract class VeinGenerator {

    // spotless:off
    public static final Codec<Codec<? extends VeinGenerator>> REGISTRY_CODEC = ResourceLocation.CODEC.flatXmap(rl -> {
        return Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.get(rl))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "No VeinGenerator with id " + rl + " registered"));
        }, obj -> {
        return Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.inverse().get(obj))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "VeinGenerator " + obj + " not registered"));
    });
    public static final Codec<VeinGenerator> DIRECT_CODEC = REGISTRY_CODEC.dispatchStable(VeinGenerator::codec, Function.identity());
    // spotless:on

    protected GTOreDefinition entry;

    public VeinGenerator() {}

    public VeinGenerator(GTOreDefinition entry) {
        this.entry = entry;
    }

    protected static void printBlockPlacementWarning(GTOreDefinition entry, String generatorName, SectionPos pos,
                                                     Direction.Axis axis, int sectionRelativeCoordinate) {
        int sectionMin = switch (axis) {
            case X -> pos.minBlockX();
            case Y -> pos.minBlockY();
            case Z -> pos.minBlockZ();
        };
        int sectionMax = switch (axis) {
            case X -> pos.maxBlockX();
            case Y -> pos.maxBlockY();
            case Z -> pos.maxBlockZ();
        };
        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
            LOGGER.warn("Tried to place block of {} vein '{}' in section ({}) outside section (at {}={}, section bounds: [{},{}])",
                    generatorName, GTRegistries.ORE_VEINS.getKey(entry), pos.toShortString(), axis.getName(),
                    sectionMin + sectionRelativeCoordinate, sectionMin, sectionMax);
        }
    }

    /**
     * @return List of [block|material, chance]
     */
    public abstract List<VeinEntry> getAllEntries();

    public List<BlockState> getAllBlocks() {
        return getAllEntries().stream()
                .map(VeinEntry::mapToBlockState)
                .toList();
    }

    public List<Material> getAllMaterials() {
        return getAllEntries().stream()
                .sorted((a, b) -> Integer.compare(b.chance, a.chance))
                .map(VeinEntry::mapToMaterial)
                .filter(Objects::nonNull)
                .toList();
    }

    public IntList getAllChances() {
        return IntArrayList.toList(getAllEntries().stream().mapToInt(VeinEntry::chance));
    }

    public List<ObjectIntPair<Material>> getValidMaterialsChances() {
        return getAllEntries().stream()
                .map(entry -> ObjectIntPair.of(entry.mapToMaterial(), entry.chance))
                .filter(pair -> pair.first() != null)
                .toList();
    }

    /**
     * Generate a map of all ore placers (by section), for each section this ore vein should be placed in.
     *
     * <p>
     * Note that, if in any way possible, this is NOT supposed to directly place any of the vein's blocks, as their
     * respective ore placers are invoked at a later time, when the chunk containing them is actually generated.
     */
    @HideFromJS
    public abstract Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random,
                                                            GTOreDefinition entry, BlockPos origin);

    @HideFromJS
    public abstract VeinGenerator build();

    public abstract VeinGenerator copy();

    @HideFromJS
    public GTOreDefinition parent() {
        return entry;
    }

    public abstract Codec<? extends VeinGenerator> codec();

    public record VeinEntry(Either<BlockState, Material> vein, int chance) {

        public static VeinEntry ofBlock(BlockState state, int chance) {
            return new VeinEntry(Either.left(state), chance);
        }

        public static VeinEntry ofMaterial(Material mat, int chance) {
            return new VeinEntry(Either.right(mat), chance);
        }

        public <T> T map(Function<BlockState, T> left, Function<Material, T> right) {
            return vein.map(left, right);
        }

        public BlockState mapToBlockState() {
            return vein.map(Function.identity(),
                    material -> ChemicalHelper.getBlockOrThrow(TagPrefix.ore, material).defaultBlockState());
        }

        public @Nullable Material mapToMaterial() {
            return vein.map(state -> ChemicalHelper.getMaterialStack(state.getBlock()).isEmpty() ? null :
                    ChemicalHelper.getMaterialStack(state.getBlock()).material(), Function.identity());
        }
    }

    public static Stream<Either<BlockState, Material>> mapTarget(Either<List<OreConfiguration.TargetBlockState>, Material> target) {
        return target.map(tbs -> tbs.stream().map(state -> Either.left(state.state)),
                mat -> Stream.of(Either.right(mat)));
    }

    public static Stream<VeinEntry> mapTarget(Either<List<OreConfiguration.TargetBlockState>, Material> target,
                                              int weight) {
        return mapTarget(target).map(entry -> new VeinEntry(entry, weight));
    }
}
