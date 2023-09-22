package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class VeinGenerator {
    public static final Codec<Codec<? extends VeinGenerator>> REGISTRY_CODEC = ResourceLocation.CODEC
            .flatXmap(rl -> Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.get(rl))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error("No VeinGenerator with id " + rl + " registered")),
                    obj -> Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.inverse().get(obj))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error("VeinGenerator " + obj + " not registered")));
    public static final Codec<VeinGenerator> DIRECT_CODEC = REGISTRY_CODEC.dispatchStable(VeinGenerator::codec, Function.identity());

    protected GTOreDefinition entry;

    public VeinGenerator() {
    }

    public VeinGenerator(GTOreDefinition entry) {
        this.entry = entry;
    }

    /**
     * @return List of [block|material, chance]
     */
    public abstract List<Map.Entry<Either<BlockState, Material>, Integer>> getAllEntries();

    public List<BlockState> getAllBlocks() {
        return getAllEntries().stream().map(entry -> entry.getKey().map(Function.identity(), material -> ChemicalHelper.getBlock(TagPrefix.ore, material).defaultBlockState())).toList();
    }

    public List<Material> getAllMaterials() {
        return getAllEntries().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .map(either -> either.map(state -> ChemicalHelper.getMaterial(state.getBlock()) != null ? ChemicalHelper.getMaterial(state.getBlock()).material() : null, Function.identity())).filter(Objects::nonNull)
                .toList();
    }

    public List<Integer> getAllChances() {
        return getAllEntries().stream().map(Map.Entry::getValue).toList();
    }

    public List<Map.Entry<Integer, Material>> getValidMaterialsChances() {
        return getAllEntries().stream()
                .filter(entry -> entry.getKey().map(state -> ChemicalHelper.getMaterial(state.getBlock()) != null ? ChemicalHelper.getMaterial(state.getBlock()).material() : null, Function.identity()) != null)
                .map(entry -> Map.entry(entry.getValue(), entry.getKey().map(state -> ChemicalHelper.getMaterial(state.getBlock()) != null ? ChemicalHelper.getMaterial(state.getBlock()).material() : null, Function.identity())))
                .collect(Collectors.toList());
    }

    @HideFromJS
    public Map<BlockPos, OreBlockPlacer> generateWithIndicator(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin) {
        var toPlace = this.generate(level, random, entry, origin);
        for (int i = 0; i < entry.getIndicatorCount(); ++i) {
            int offsetX = random.nextInt(-4, 4);
            int offsetY = random.nextInt(-4, 4);
            int offsetZ = random.nextInt(-4, 4);
            var materials = entry.getBedrockVeinMaterials();
            int index = GTUtil.getRandomItem(random, materials, materials.size());
            if (index == -1) continue;
            Material material = materials.get(index).getValue();
            BlockPos current = origin.offset(offsetX, offsetY, offsetZ);
            OreBlockPlacer possible = entry.getIndicatorType().getPossiblePositions(level, random, current, 8, GTBlocks.SURFACE_ROCK_BLOCKS.get(material).getDefaultState());
            toPlace.putIfAbsent(current, possible);
        }
        return toPlace;
    }

    @HideFromJS
    public abstract Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin);

    @HideFromJS
    public abstract VeinGenerator build();

    public abstract VeinGenerator copy();

    @HideFromJS
    public GTOreDefinition parent() {
        return entry;
    }

    public abstract Codec<? extends VeinGenerator> codec();
}
