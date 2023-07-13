package com.gregtechceu.gtceu.api.data.worldgen.bedrockore;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * @author KilaBash
 * @date 2023/7/11
 * @implNote BedrockFluidVeinSavedData
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BedrockOreVeinSavedData extends SavedData {
    public static final int VEIN_CHUNK_SIZE = 3; // veins are 3x3 chunk squares
    public static final int MAXIMUM_VEIN_OPERATIONS = 100_000;
    public final HashMap<ChunkPos, OreVeinWorldEntry> veinOres = new HashMap<>();

    // runtime
    private final HashMap<Holder<Biome>, Integer> biomeWeights = new HashMap<>();

    private final ServerLevel serverLevel;

    public static BedrockOreVeinSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new BedrockOreVeinSavedData(serverLevel, tag), () -> new BedrockOreVeinSavedData(serverLevel), "gtceu_bedrock_ore");
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

    /**
     * Gets the FluidVeinWorldInfo object associated with the given chunk
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return The FluidVeinWorldInfo corresponding with the given chunk
     */
    public OreVeinWorldEntry getOreVeinWorldEntry(int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if (!veinOres.containsKey(pos)) {
            GTOreFeatureEntry definition = null;
            int query = RandomSource.create(Objects.hash(96548, chunkX / VEIN_CHUNK_SIZE, chunkZ / VEIN_CHUNK_SIZE)).nextInt();
            var biome = serverLevel.getBiome(new BlockPos(chunkX << 4, 64, chunkZ << 4));
            int totalWeight = getTotalWeight(biome);
            if (totalWeight > 0) {
                int weight = Math.abs(query % totalWeight);
                for (var oreDefinition : GTRegistries.ORE_VEINS) {
                    int veinWeight = oreDefinition.getWeight() + oreDefinition.getBiomeWeightModifier().apply(biome);
                    if (veinWeight > 0 && oreDefinition.getDimensionFilter().contains(serverLevel.dimensionTypeRegistration())) {
                        weight -= veinWeight;
                        if (weight < 0) {
                            definition = oreDefinition;
                            break;
                        }
                    }
                }
            }

            var random = RandomSource.create(31L * 31 * chunkX + chunkZ * 31L + Long.hashCode(serverLevel.getSeed()));

            int maximumYield = 0;
            if (definition != null) {
                if (definition.getMaximumYield() - definition.getMinimumYield() <= 0) {
                    maximumYield = definition.getMinimumYield();
                } else {
                    maximumYield = random.nextInt(definition.getMaximumYield() - definition.getMinimumYield()) + definition.getMinimumYield();
                }
                maximumYield = Math.min(maximumYield, definition.getMaximumYield());
            }
            veinOres.put(new ChunkPos(chunkX, chunkZ), new OreVeinWorldEntry(definition, maximumYield, MAXIMUM_VEIN_OPERATIONS));
            setDirty();
        }
        return veinOres.get(pos);
    }

    public void createVein(ChunkPos pos, GTOreFeatureEntry definition) {
        ChunkPos original = pos;
        for (int x = pos.x - 1, z = pos.z - 1; x < pos.x + 1 && z < pos.z + 1; ++x, ++z) {
            pos = new ChunkPos(x, z);
            if (!veinOres.containsKey(pos)) {
                float distanceFromOriginal = original.getChessboardDistance(pos);
                distanceFromOriginal /= Math.max(distanceFromOriginal * 2, 1);

                var random = RandomSource.create(31L * 31 * pos.x + pos.z * 31L + Long.hashCode(serverLevel.getSeed()));

                int maximumYield = 0;
                if (definition != null) {
                    if ((definition.getMaximumYield() - definition.getMinimumYield()) * distanceFromOriginal <= 0) {
                        maximumYield = definition.getMinimumYield();
                    } else {
                        maximumYield = (int) (random.nextInt(definition.getMaximumYield() - definition.getMinimumYield()) + definition.getMinimumYield() * distanceFromOriginal);
                    }
                    maximumYield = Math.min(maximumYield, definition.getMaximumYield());
                }
                veinOres.put(pos, new OreVeinWorldEntry(definition, maximumYield, MAXIMUM_VEIN_OPERATIONS));
            }
        }

    }

    /**
     * Gets the total weight of all veins for the given dimension ID and biome type
     *
     * @param biome    The biome type to check
     * @return The total weight associated with the dimension/biome pair
     */
    public int getTotalWeight(Holder<Biome> biome) {
        return biomeWeights.computeIfAbsent(biome, b -> {
            int totalWeight = 0;
            for (var definition : GTRegistries.ORE_VEINS) {
                if (definition.getDimensionFilter().contains(serverLevel.dimensionTypeRegistration())) {
                    totalWeight += definition.getBiomeWeightModifier().apply(biome);
                    totalWeight += definition.getWeight();
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
        return info.getDefinition().getDepletedYield();
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
    public List<Map.Entry<Integer, Material>> getOreInChunk(int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(chunkX, chunkZ);
        if (info.getDefinition() == null) return null;
        return info.getDefinition().getBedrockVeinMaterials();
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

        GTOreFeatureEntry definition = info.getDefinition();

        // prevent division by zero, veins that never deplete don't need updating
        if (definition == null || definition.getDepletionChance() == 0)
            return;

        if (definition.getDepletionChance() == 100 || GTValues.RNG.nextInt(100) <= definition.getDepletionChance()) {
            info.decreaseOperations(definition.getDepletionAmount());
            setDirty();
        }
    }

}
