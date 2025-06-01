package com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.SavedData;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BedrockFluidVeinSavedData extends SavedData {

    public static final int VEIN_CHUNK_SIZE = 8; // veins are 8x8 chunk squares
    public static final int MAXIMUM_VEIN_OPERATIONS = 100_000;
    public final HashMap<ChunkPos, FluidVeinWorldEntry> veinFluids = new HashMap<>();

    // runtime
    private final Object2IntMap<Holder<Biome>> biomeWeights = new Object2IntOpenHashMap<>();

    private final ServerLevel serverLevel;

    public static BedrockFluidVeinSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new BedrockFluidVeinSavedData(serverLevel, tag),
                () -> new BedrockFluidVeinSavedData(serverLevel), "gtceu_bedrock_fluid");
    }

    public BedrockFluidVeinSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public BedrockFluidVeinSavedData(ServerLevel serverLevel, CompoundTag nbt) {
        this(serverLevel);
        var list = nbt.getList("veinInfo", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag compoundTag = list.getCompound(i);
            var chunkPos = new ChunkPos(compoundTag.getLong("p"));
            veinFluids.put(chunkPos, FluidVeinWorldEntry.readFromNBT(compoundTag.getCompound("d")));
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        var oilList = new ListTag();
        for (var entry : veinFluids.entrySet()) {
            var tag = new CompoundTag();
            tag.putLong("p", entry.getKey().toLong());
            tag.put("d", entry.getValue().writeToNBT());
            oilList.add(tag);
        }
        nbt.put("veinInfo", oilList);
        return nbt;
    }

    public static int getVeinCoord(int chunkCoord) {
        return Math.floorDiv(chunkCoord, VEIN_CHUNK_SIZE);
    }

    /**
     * Gets the FluidVeinWorldInfo object associated with the given chunk
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return The FluidVeinWorldInfo corresponding with the given chunk
     */
    public FluidVeinWorldEntry getFluidVeinWorldEntry(int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if (!veinFluids.containsKey(pos)) {
            BedrockFluidDefinition definition = null;
            int query = RandomSource
                    .create(GTMath.hashLongs(serverLevel.getSeed(), getVeinCoord(chunkX), getVeinCoord(chunkZ)))
                    .nextInt();
            var biome = serverLevel.getBiome(new BlockPos(chunkX << 4, 64, chunkZ << 4));
            int totalWeight = getTotalWeight(biome);
            if (totalWeight > 0) {
                int weight = Math.abs(query % totalWeight);
                for (var fluidDefinition : GTRegistries.BEDROCK_FLUID_DEFINITIONS) {
                    int veinWeight = fluidDefinition.getWeight() +
                            fluidDefinition.getBiomeWeightModifier().applyAsInt(biome);
                    if (veinWeight > 0 && (fluidDefinition.getDimensionFilter() == null ||
                            fluidDefinition.getDimensionFilter().stream().anyMatch(
                                    dim -> WorldGeneratorUtils.isSameDimension(dim, serverLevel.dimension())))) {
                        weight -= veinWeight;
                        if (weight < 0) {
                            definition = fluidDefinition;
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
                    maximumYield = random.nextInt(definition.getMaximumYield() - definition.getMinimumYield()) +
                            definition.getMinimumYield();
                }
                maximumYield = Math.min(maximumYield, definition.getMaximumYield());
            }
            veinFluids.put(pos, new FluidVeinWorldEntry(definition, maximumYield, MAXIMUM_VEIN_OPERATIONS));
            setDirty();
        }
        return veinFluids.get(pos);
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
            for (var definition : GTRegistries.BEDROCK_FLUID_DEFINITIONS) {
                if (definition.getDimensionFilter() == null || definition.getDimensionFilter().stream()
                        .anyMatch(dim -> WorldGeneratorUtils.isSameDimension(dim, serverLevel.dimension()))) {
                    totalWeight += definition.getBiomeWeightModifier().applyAsInt(biome);
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
    public int getFluidYield(int chunkX, int chunkZ) {
        return getFluidVeinWorldEntry(chunkX, chunkZ).getFluidYield();
    }

    /**
     * Gets the yield of fluid in the chunk after the vein is completely depleted
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return yield of fluid post depletion
     */
    public int getDepletedFluidYield(int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(chunkX, chunkZ);
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
        return getFluidVeinWorldEntry(chunkX, chunkZ).getOperationsRemaining();
    }

    /**
     * Gets the Fluid in a specific chunk's vein
     *
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return Fluid in given chunk
     */
    @Nullable
    public Fluid getFluidInChunk(int chunkX, int chunkZ) {
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(chunkX, chunkZ);
        if (info.getDefinition() == null) return null;
        return info.getDefinition().getStoredFluid().get();
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
        FluidVeinWorldEntry info = getFluidVeinWorldEntry(chunkX, chunkZ);

        if (ignoreVeinStats) {
            info.decreaseOperations(amount);
            if (amount != 0) {
                setDirty();
            }
            return;
        }

        BedrockFluidDefinition definition = info.getDefinition();

        // prevent division by zero, veins that never deplete don't need updating
        if (definition == null || definition.getDepletionChance() == 0)
            return;

        if (definition.getDepletionChance() == 100 || GTValues.RNG.nextInt(100) <= definition.getDepletionChance()) {
            info.decreaseOperations(definition.getDepletionAmount());
            setDirty();
        }
    }
}
