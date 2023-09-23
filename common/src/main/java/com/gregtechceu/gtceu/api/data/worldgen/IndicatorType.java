package com.gregtechceu.gtceu.api.data.worldgen;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FloodFiller3D;
import com.mojang.serialization.Codec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@MethodsReturnNonnullByDefault
public enum IndicatorType implements StringRepresentable {
    /**
     * No indicator
     */
    NONE {
        @Override
        public OreBlockPlacer getPossiblePositions(LevelReader level, RandomSource random, BlockPos start, int searchRadius, BlockState toPlace) {
            return (access, section) -> {};
        }
    },
    /**
     * Topmost block in pos
     */
    SURFACE {
        @Override
        public OreBlockPlacer getPossiblePositions(LevelReader level, RandomSource random, BlockPos start, int searchRadius, BlockState toPlace) {
            try {
                return this.resolvedPositions.get(start, () -> {
                    return (access, section) -> {
                        int height = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, start.getX(), start.getZ());
                        int sectionX = SectionPos.sectionRelative(start.getX());
                        int sectionY = SectionPos.sectionRelative(height);
                        int sectionZ = SectionPos.sectionRelative(start.getZ());

                        section.setBlockState(sectionX, sectionY, sectionZ, toPlace, false);
                    };
                });

            } catch (
                    ExecutionException e) {
                GTCEu.LOGGER.error("failed to check indicator at " + start);
                return (access, section) -> {};
            }
        }
    },
    /**
     * Nearest empty bloc
     */
    NEAREST_EMPTY {
        @Override
        public OreBlockPlacer getPossiblePositions(LevelReader level, RandomSource random, BlockPos start, int searchRadius, BlockState toPlace) {
            try {
                return this.resolvedPositions.get(start, () -> {
                    return (access, section) -> {
                        for (int y = start.getY(); y < level.getMaxBuildHeight(); ++y) {
                            int sectionX = SectionPos.sectionRelative(start.getX());
                            int sectionY = SectionPos.sectionRelative(y);
                            int sectionZ = SectionPos.sectionRelative(start.getZ());
                            if (!section.getBlockState(sectionX, sectionY, sectionZ).getMaterial().blocksMotion()) {
                                section.setBlockState(sectionX, sectionY, sectionZ, toPlace, false);
                                break;
                            }
                        }
                    };
                });

            } catch (ExecutionException e) {
                GTCEu.LOGGER.error("failed to check indicator at " + start);
                return (access, section) -> {};
            }
        }
    },
    /**
     * Largest nearby empty space
     * If no empty space exists, generate on surface
     */
    LARGEST_EMPTY {
        @Override
        public OreBlockPlacer getPossiblePositions(LevelReader level, RandomSource random, BlockPos start, int searchRadius, BlockState toPlace) {
            try {
                return this.resolvedPositions.get(start, () -> {
                    AABB area = new AABB(start.offset(-searchRadius, 0, -searchRadius), start.offset(searchRadius, searchRadius, searchRadius));
                    return ((access, section) -> {
                        Set<BlockPos> largest = Set.of();
                        for (BlockPos pos : BlockPos.randomInCube(random, 10, start.above(searchRadius / 2), searchRadius)) {
                            var positions = FloodFiller3D.run(level, pos);
                            if (positions.size() > largest.size()) {
                                largest = positions;
                            }
                        }
                        for (BlockPos pos : largest) {
                            if (area.contains(pos.getX(), pos.getY(), pos.getZ()) && level.getBlockState(pos.relative(Direction.DOWN)).getMaterial().blocksMotion()) {
                                int sectionX = SectionPos.sectionRelative(pos.getX());
                                int sectionY = SectionPos.sectionRelative(pos.getY());
                                int sectionZ = SectionPos.sectionRelative(pos.getZ());
                                section.setBlockState(sectionX, sectionY, sectionZ, toPlace, false);
                                break;
                            }
                        }
                    });
                });

            } catch (ExecutionException e) {
                GTCEu.LOGGER.error("failed to check indicator at " + start);
                return (access, section) -> {};
            }
        }
    },
    ;

    public static final Codec<IndicatorType> CODEC = StringRepresentable.fromEnum(IndicatorType::values);

    final Cache<BlockPos, OreBlockPlacer> resolvedPositions = CacheBuilder.newBuilder()
            .maximumSize(ConfigHolder.INSTANCE != null ? ConfigHolder.INSTANCE.worldgen.oreVeins.oreGenerationChunkCacheSize : 512)
            .softValues()
            .build();

    public abstract OreBlockPlacer getPossiblePositions(LevelReader level, RandomSource random, BlockPos start, int searchRadius, BlockState toPlace);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
