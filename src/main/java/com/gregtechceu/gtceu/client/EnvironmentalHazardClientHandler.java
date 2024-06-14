package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.particle.HazardParticleOptions;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.ClientLevelAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData.MIN_STRENGTH_FOR_SPREAD;

@OnlyIn(Dist.CLIENT)
public class EnvironmentalHazardClientHandler {

    public static final int MAX_PARTICLE_DISTANCE = 48;
    public static final int MAX_PARTICLE_DISTANCE_SQR = MAX_PARTICLE_DISTANCE * MAX_PARTICLE_DISTANCE;

    public static final int COLORING_LOW = 350;
    public static final int COLORING_HIGH = 600;
    private static final int[] GRASS_COLORS = { 230, 180, 40 };
    private static final int[] LIQUID_COLORS = { 160, 200, 10 };
    private static final int[] FOLIAGE_COLORS = { 160, 80, 15 };

    public static final EnvironmentalHazardClientHandler INSTANCE = new EnvironmentalHazardClientHandler();

    private EnvironmentalHazardClientHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Map of source position to a triple of (trigger, material).
     */
    @Getter
    private final Map<ChunkPos, EnvironmentalHazardSavedData.HazardZone> hazardZones = new HashMap<>();

    public void onClientTick() {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RandomSource random = level.random;
        BlockPos playerPosition = BlockPos.containing(Minecraft.getInstance().player.getEyePosition());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (var entry : hazardZones.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                continue;
            }
            var zone = entry.getValue();
            if (zone.strength() < MIN_STRENGTH_FOR_SPREAD / 5) {
                continue;
            }
            BlockPos source = entry.getKey().getMiddleBlockPosition(zone.source().getY());
            if (playerPosition.distSqr(source) > MAX_PARTICLE_DISTANCE_SQR) {
                continue;
            }

            for (int i = 0; i < 32; ++i) {
                // random is slightly over 8 (half a chunk) so that the particles cover the chunk better.
                // in my testing this didn't spill over too much.
                int randX = source.getX() - random.nextInt(9) + random.nextInt(9);
                int randY = source.getY() - random.nextInt(9) + random.nextInt(9);
                int randZ = source.getZ() - random.nextInt(9) + random.nextInt(9);
                pos.set(randX, randY, randZ);
                if (!level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
                    level.addParticle(
                            new HazardParticleOptions(zone.condition().color, zone.strength() / 250f),
                            pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                            pos.getZ() + random.nextDouble(),
                            0, 0, 0);
                }
            }
        }
    }

    public void updateHazardMap(Map<ChunkPos, EnvironmentalHazardSavedData.HazardZone> newZones) {
        this.hazardZones.clear();
        this.hazardZones.putAll(newZones);
        // must clear tint caches when block colors change
        for (var entry : newZones.entrySet()) {
            if (entry.getValue().strength() > COLORING_LOW) {
                ChunkPos pos = entry.getKey();
                ((ClientLevelAccessor) Minecraft.getInstance().level).getTintCaches()
                        .forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateForChunk(pos.x, pos.z));
            }
        }
    }

    public void updateHazardStrength(ChunkPos pos, int newStrength) {
        hazardZones.computeIfPresent(pos, (key, zone) -> new EnvironmentalHazardSavedData.HazardZone(zone.source(),
                newStrength,
                zone.canSpread(),
                zone.trigger(),
                zone.condition()));
        // must clear tint caches when block colors change
        if (newStrength > COLORING_LOW) {
            ((ClientLevelAccessor) Minecraft.getInstance().level).getTintCaches()
                    .forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateForChunk(pos.x, pos.z));
        }
    }

    public void addHazardZone(ChunkPos pos, EnvironmentalHazardSavedData.HazardZone zone) {
        this.hazardZones.put(pos, zone);
        // must clear tint caches when block colors change
        if (zone.strength() > COLORING_LOW) {
            ((ClientLevelAccessor) Minecraft.getInstance().level).getTintCaches()
                    .forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateForChunk(pos.x, pos.z));
        }
    }

    public void removeHazardZone(ChunkPos pos) {
        this.hazardZones.remove(pos);
    }

    public int colorGrass(int color, ChunkPos pos) {
        var zone = hazardZones.get(pos);
        if (zone == null) {
            return color;
        }
        return colorize(color, zone.strength(), zone.condition().color, GRASS_COLORS);
    }

    public int colorLiquid(int color, ChunkPos pos) {
        var zone = hazardZones.get(pos);
        if (zone == null) {
            return color;
        }
        return colorize(color, zone.strength(), zone.condition().color, LIQUID_COLORS);
    }

    public int colorFoliage(int color, ChunkPos pos) {
        var zone = hazardZones.get(pos);
        if (zone == null) {
            return color;
        }
        return colorize(color, zone.strength(), zone.condition().color, FOLIAGE_COLORS);
    }

    /**
     * @param color     the existing color to modify
     * @param pollution the amount of pollution present
     * @param newColor  the color to interpolate with
     * @param toMix     the colors to interpolate between
     * @return the new color
     */
    private static int colorize(int color, int pollution, int newColor, int[] toMix) {
        if (pollution < COLORING_LOW) return color;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        float ratio = ((float) (pollution - COLORING_LOW)) / COLORING_HIGH;
        if (ratio > 1) ratio = 1;

        float complement = 1 - ratio;

        r = ((int) (r * complement + ratio * FastColor.ARGB32.red(newColor)) * toMix[0]) & 0xFF;
        g = ((int) (g * complement + ratio * FastColor.ARGB32.green(newColor)) * toMix[1]) & 0xFF;
        b = ((int) (b * complement + ratio * FastColor.ARGB32.blue(newColor)) * toMix[2]) & 0xFF;

        return FastColor.ARGB32.color(0xFF, r, g, b);
    }
}
