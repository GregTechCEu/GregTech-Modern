package com.gregtechceu.gtceu.api.data.worldgen.bedrockore;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BedrockOreVeinSavedData extends SavedData {

    public static final int VEIN_CHUNK_SIZE = 3; // veins are 3x3 chunk squares
    public static final int MAXIMUM_VEIN_OPERATIONS = 100_000;
    public final HashMap<ChunkPos, OreVeinWorldEntry> veinOres = new HashMap<>();

    // runtime
    private final Object2IntMap<Holder<Biome>> biomeWeights = new Object2IntOpenHashMap<>();

    private final ServerLevel serverLevel;

    public static BedrockOreVeinSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new BedrockOreVeinSavedData(serverLevel, tag),
                () -> new BedrockOreVeinSavedData(serverLevel), "gtceu_bedrock_ore");
    }

    public BedrockOreVeinSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public BedrockOreVeinSavedData(ServerLevel serverLevel, CompoundTag nbt) {
        this(serverLevel);
        var list = nbt.getList("veinInfo", Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            if (tag instanceof CompoundTag compoundTag) {
                var chunkPos = new ChunkPos(compoundTag.getLong("pos"));
                veinOres.put(chunkPos, OreVeinWorldEntry.readFromNBT(compoundTag.getCompound("data")));
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        var oreList = new ListTag();
        for (var entry : veinOres.entrySet()) {
            var tag = new CompoundTag();
            tag.putLong("pos", entry.getKey().toLong());
            tag.put("data", entry.getValue().writeToNBT());
            oreList.add(tag);
        }
        nbt.put("veinInfo", oreList);
        return nbt;
    }

    public static int getVeinCoord(int chunkCoord) {
        return Math.floorDiv(chunkCoord, VEIN_CHUNK_SIZE);
    }

    /**
     * Gets the OreVeinWorldEntry object associated with the given chunk
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return The OreVeinWorldEntry corresponding with the given chunk
     */
    public OreVeinWorldEntry getOreVeinWorldEntry(int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if (!veinOres.containsKey(pos)) {
            int minDistance = ConfigHolder.INSTANCE.worldgen.oreVeins.bedrockOreDistance;
            if (chunkX % minDistance != 0 || chunkZ % minDistance != 0) {
                OreVeinWorldEntry entry = new OreVeinWorldEntry(null, 0, MAXIMUM_VEIN_OPERATIONS);
                veinOres.put(pos, entry);
                return entry;
            }

            BedrockOreDefinition definition = null;
            int query = RandomSource
                    .create(GTMath.hashLongs(serverLevel.getSeed(), getVeinCoord(chunkX), getVeinCoord(chunkZ)))
                    .nextInt();
            var biome = serverLevel.getBiome(new BlockPos(chunkX << 4, 64, chunkZ << 4));
            int totalWeight = getTotalWeight(biome);
            if (totalWeight > 0) {
                int weight = Math.abs(query % totalWeight);
                for (var oreDefinition : GTRegistries.BEDROCK_ORE_DEFINITIONS) {
                    int veinWeight = oreDefinition.weight() + (oreDefinition.biomeWeightModifier() != null ?
                            oreDefinition.biomeWeightModifier().applyAsInt(biome) : 0);
                    if (veinWeight > 0 &&
                            (oreDefinition.dimensionFilter == null || oreDefinition.dimensionFilter().stream().anyMatch(
                                    dim -> WorldGeneratorUtils.isSameDimension(dim, serverLevel.dimension())))) {
                        weight -= veinWeight;
                        if (weight < 0) {
                            definition = oreDefinition;
                            break;
                        }
                    }
                }
            }

            createVein(pos, definition);
            setDirty();
        }
        if (!veinOres.containsKey(pos)) {
            OreVeinWorldEntry entry = new OreVeinWorldEntry(null, 0, MAXIMUM_VEIN_OPERATIONS);
            veinOres.put(pos, entry);
            return entry;
        }
        return veinOres.get(pos);
    }

    public void createVein(ChunkPos pos, @Nullable BedrockOreDefinition definition) {
        if (definition != null) {
            int radius = definition.size() / 2;
            for (int x = pos.x - radius; x <= pos.x + radius; ++x) {
                for (int z = pos.z - radius; z <= pos.z + radius; ++z) {
                    ChunkPos pos2 = new ChunkPos(x, z);
                    float distanceFromOriginal = Math.abs(pos.x - x) + Math.abs(pos.z - z);
                    distanceFromOriginal = distanceFromOriginal == 0 ? 1 : distanceFromOriginal;
                    distanceFromOriginal = (float) Math.pow(distanceFromOriginal, 2);

                    var random = RandomSource
                            .create(31L * 31 * pos2.x + pos2.z * 31L + Long.hashCode(serverLevel.getSeed()));

                    int maximumYield;
                    if ((definition.yield().getMaxValue() - definition.yield().getMinValue()) / distanceFromOriginal <=
                            0) {
                        maximumYield = definition.yield().getMinValue();
                    } else {
                        maximumYield = (int) ((definition.yield().sample(random) + definition.yield().getMinValue()) /
                                distanceFromOriginal);
                        maximumYield = Math.max(maximumYield, definition.yield().getMinValue());
                    }
                    maximumYield = Math.min(maximumYield, definition.yield().getMaxValue());

                    veinOres.put(pos2, new OreVeinWorldEntry(definition, maximumYield, MAXIMUM_VEIN_OPERATIONS));
                }
            }
        }
    }

    /**
     * Gets the total weight of all veins for the given dimension ID and biome type
     *
     * @param biome The biome type to check
     * @return The total weight associated with the dimension/biome pair
     */
    public int getTotalWeight(Holder<Biome> biome) {
        return biomeWeights.computeIfAbsent(biome, b -> {
            int totalWeight = 0;
            for (var definition : GTRegistries.BEDROCK_ORE_DEFINITIONS) {
                if (definition.dimensionFilter == null || definition.dimensionFilter().stream()
                        .anyMatch(dim -> WorldGeneratorUtils.isSameDimension(dim, serverLevel.dimension()))) {
                    totalWeight += definition.biomeWeightModifier() != null ?
                            definition.biomeWeightModifier().applyAsInt(biome) : 0;
                    totalWeight += definition.weight();
                }
            }
            return totalWeight;
        });
    }

    /**
     * gets the fluid yield in a specific chunk
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return yield in the vein
     */
    public int getOreYield(int chunkX, int chunkZ) {
        return getOreVeinWorldEntry(chunkX, chunkZ).getOreYield();
    }

    /**
     * Gets the yield of fluid in the chunk after the vein is completely depleted
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return yield of fluid post depletion
     */
    public int getDepletedOreYield(int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(chunkX, chunkZ);
        if (info.getDefinition() == null) return 0;
        return info.getDefinition().depletedYield();
    }

    /**
     * Gets the current operations remaining in a specific chunk's vein
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return amount of operations in the given chunk
     */
    public int getOperationsRemaining(int chunkX, int chunkZ) {
        return getOreVeinWorldEntry(chunkX, chunkZ).getOperationsRemaining();
    }

    /**
     * Gets the Fluid in a specific chunk's vein
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return Fluid in given chunk
     */
    @Nullable
    public List<WeightedMaterial> getOreInChunk(int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(chunkX, chunkZ);
        if (info.getDefinition() == null) return null;
        return info.getDefinition().materials();
    }

    /**
     * Depletes fluid from a given chunk
     *
     * @param chunkX          Chunk x
     * @param chunkZ          Chunk z
     * @param amount          the amount of fluid to deplete the vein by
     * @param ignoreVeinStats whether to ignore the vein's depletion data, if false ignores amount
     */
    public void depleteVein(int chunkX, int chunkZ, int amount, boolean ignoreVeinStats) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(chunkX, chunkZ);

        if (ignoreVeinStats) {
            info.decreaseOperations(amount);
            if (amount != 0) {
                setDirty();
            }
            return;
        }

        BedrockOreDefinition definition = info.getDefinition();

        // prevent division by zero, veins that never deplete don't need updating
        if (definition == null || definition.depletionChance() == 0)
            return;

        if (definition.depletionChance() == 100 || GTValues.RNG.nextInt(100) <= definition.depletionChance()) {
            info.decreaseOperations(definition.depletionAmount());
            setDirty();
        }
    }
}
