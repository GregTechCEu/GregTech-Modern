package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardCleaner;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import lombok.Getter;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VHA;

public class AirScrubberMachine extends SimpleTieredMachine implements IEnvironmentalHazardCleaner {

    public static final float MIN_CLEANING_PER_OPERATION = 10;

    private final float cleaningPerOperation;

    @Getter
    private float removedLastSecond;

    public AirScrubberMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachines.largeTankSizeFunction, args);
        this.cleaningPerOperation = MIN_CLEANING_PER_OPERATION * tier;
    }

    @Override
    public boolean dampingWhenWaiting() {
        return false;
    }

    @Override
    public void cleanHazard(MedicalCondition condition, float amount) {
        if (this.recipeLogic.isActive()) {
            return;
        }

        GTRecipeBuilder builder = GTRecipeTypes.AIR_SCRUBBER_RECIPES.recipeBuilder(condition.name + "_autogen")
                .duration(200).EUt(VHA[LV]);
        condition.recipeModifier.accept(builder);
        this.recipeLogic.checkMatchedRecipeAvailable(builder.buildRawRecipe());
    }

    @Override
    public boolean onWorking() {
        if (!super.onWorking()) {
            return false;
        }
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return true;
        }

        if (getOffsetTimer() % 20 == 0) {
            removedLastSecond = 0;

            for (Direction dir : GTUtil.DIRECTIONS) {
                BlockPos offset = getPos().relative(dir);
                if (GTCapabilityHelper.getHazardContainer(getLevel(), offset, dir.getOpposite()) != null) {
                    if (getLevel().getBlockEntity(offset) instanceof PipeBlockEntity duct &&
                            !duct.isConnected(dir.getOpposite())) {
                        continue;
                    }
                    return true;
                }
            }

            final ServerLevel serverLevel = (ServerLevel) getLevel();
            EnvironmentalHazardSavedData savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);

            final ChunkPos pos = new ChunkPos(getPos());
            Object2FloatMap<ChunkPos> relativePositions = new Object2FloatOpenHashMap<>();
            int radius = tier / 2;
            if (radius <= 0) {
                // LV scrubber can only process the chunk it's in
                relativePositions.put(pos, 1);
            } else {
                for (int x = -radius; x <= radius; ++x) {
                    for (int z = -radius; z <= radius; ++z) {
                        relativePositions.put(new ChunkPos(pos.x + x, pos.z + z), Mth.sqrt(Mth.abs(x * z)) + 1);
                    }
                }
            }
            for (ChunkPos rel : relativePositions.keySet()) {
                final float distance = relativePositions.getFloat(rel);
                savedData.getHazardZones().compute(rel, (chunkPos, zone) -> {
                    if (zone == null || zone.strength() <= 0) {
                        return null;
                    }

                    float toClean = cleaningPerOperation / distance;
                    removedLastSecond += toClean;
                    zone.removeStrength(toClean);
                    if (zone.strength() <= 0) {
                        if (serverLevel.hasChunk(chunkPos.x, chunkPos.z)) {
                            LevelChunk chunk = serverLevel.getChunk(chunkPos.x, chunkPos.z);
                            GTNetwork.NETWORK.sendToTrackingChunk(new SPacketRemoveHazardZone(chunkPos), chunk);
                        }
                        return null;
                    } else return zone;
                });
            }
        }
        return true;
    }
}
