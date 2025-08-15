package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.particle.HazardParticleOptions;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.client.ClientLevelAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import it.unimi.dsi.fastutil.floats.FloatIntPair;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class EnvironmentalHazardClientHandler {

    public static final int PARTICLE_THRESHOLD = 200;
    public static final int MAX_PARTICLE_DISTANCE = 96;
    public static final int MAX_PARTICLE_DISTANCE_SQR = MAX_PARTICLE_DISTANCE * MAX_PARTICLE_DISTANCE;

    public static final float COLORING_LOW = PARTICLE_THRESHOLD;
    public static final float COLORING_HIGH = 600;

    public static final EnvironmentalHazardClientHandler INSTANCE = new EnvironmentalHazardClientHandler();

    private EnvironmentalHazardClientHandler() {
        if (ConfigHolder.INSTANCE.gameplay.hazardsEnabled) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    /**
     * Map of source position to a triple of (trigger, material).
     */
    @Getter
    private final Map<ChunkPos, EnvironmentalHazardSavedData.HazardZone> hazardZones = new ConcurrentHashMap<>();
    private final Map<ChunkPos, FloatIntPair> chunkColorCache = new ConcurrentHashMap<>();

    public void onClientTick() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RandomSource random = level.random;
        Vec3 playerPosition = Minecraft.getInstance().player.getEyePosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (var entry : hazardZones.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                continue;
            }
            var zone = entry.getValue();
            if (zone.strength() < PARTICLE_THRESHOLD) {
                continue;
            }
            BlockPos source = chunkPos.getMiddleBlockPosition(zone.source().getY());
            if (source.distToCenterSqr(playerPosition) > MAX_PARTICLE_DISTANCE_SQR) {
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
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        this.hazardZones.clear();
        this.hazardZones.putAll(newZones);
        // must clear tint caches when block colors change
        for (var entry : newZones.entrySet()) {
            if (entry.getValue().strength() > COLORING_LOW) {
                ChunkPos pos = entry.getKey();
                for (int y = Minecraft.getInstance().level.getMinSection(); y <
                        Minecraft.getInstance().level.getMaxSection(); ++y) {
                    Minecraft.getInstance().levelRenderer.setSectionDirtyWithNeighbors(pos.x, y, pos.z);
                }

                ((ClientLevelAccessor) Minecraft.getInstance().level).getTintCaches()
                        .forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateForChunk(pos.x, pos.z));
            }
        }
    }

    public void updateHazardStrength(ChunkPos pos, float newStrength) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        if (hazardZones.containsKey(pos)) {
            hazardZones.get(pos).strength(newStrength);
        }
        // must clear tint caches when block colors change
        if (newStrength > COLORING_LOW) {
            updateChunks(pos);
        }
    }

    public void addHazardZone(ChunkPos pos, EnvironmentalHazardSavedData.HazardZone zone) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        this.hazardZones.put(pos, zone);
        // must clear tint caches when block colors change
        if (zone.strength() > COLORING_LOW) {
            updateChunks(pos);
        }
    }

    public void removeHazardZone(ChunkPos pos) {
        this.hazardZones.remove(pos);

        updateChunks(pos);
    }

    private void updateChunks(ChunkPos pos) {
        for (int y = Minecraft.getInstance().level.getMinSection(); y <
                Minecraft.getInstance().level.getMaxSection(); ++y) {
            Minecraft.getInstance().levelRenderer.setSectionDirtyWithNeighbors(pos.x, y, pos.z);
        }

        ((ClientLevelAccessor) Minecraft.getInstance().level).getTintCaches()
                .forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateForChunk(pos.x, pos.z));
    }

    public int colorZone(int color, ChunkPos pos) {
        var zone = hazardZones.get(pos);
        if (zone == null) {
            return color;
        }
        var entry = chunkColorCache.get(pos);
        if (entry != null &&
                (entry.firstFloat() > zone.strength() + 0.5f || entry.firstFloat() < zone.strength() - 0.5f)) {
            return entry.valueInt();
        }

        color = colorize(color, zone.strength(), zone.condition().color);
        chunkColorCache.put(pos, FloatIntPair.of(zone.strength(), color));
        return color;
    }

    /**
     * @param color     the existing color to modify
     * @param pollution the amount of pollution present
     * @param newColor  the color to interpolate with
     * @return the new color
     */
    private static int colorize(int color, float pollution, int newColor) {
        if (pollution < COLORING_LOW) return color;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        float ratio = (pollution - COLORING_LOW) / COLORING_HIGH;
        if (ratio > 1) ratio = 1;

        float complement = 1 - ratio;

        r = ((int) (r * complement + ratio * FastColor.ARGB32.red(newColor))) & 0xFF;
        g = ((int) (g * complement + ratio * FastColor.ARGB32.green(newColor))) & 0xFF;
        b = ((int) (b * complement + ratio * FastColor.ARGB32.blue(newColor))) & 0xFF;

        return FastColor.ARGB32.color(0xFF, r, g, b);
    }
}
