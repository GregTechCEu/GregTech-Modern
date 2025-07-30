package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardCleaner;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.DuctPipeBlockEntity;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
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
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VHA;

public class AirScrubberMachine extends SimpleTieredMachine implements IEnvironmentalHazardCleaner {

    public static final float MIN_CLEANING_PER_OPERATION = 10;

    private float cleaningPerOperation;

    @Getter
    private float removedLastSecond;

    public AirScrubberMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachineUtils.largeTankSizeFunction, args);
        this.cleaningPerOperation = MIN_CLEANING_PER_OPERATION;
    }

    @Override
    public boolean regressWhenWaiting() {
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
    public boolean isRecipeLogicAvailable() {
        // Don't run recipes if hazards are off
        return ConfigHolder.INSTANCE.gameplay.environmentalHazards;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (super.beforeWorking(recipe) && recipe != null) {
            // Sets the amount of hazard to clean based on the recipe tier, not the machine tier
            this.cleaningPerOperation = MIN_CLEANING_PER_OPERATION * (recipe.ocLevel + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onWorking() {
        if (!super.onWorking() || !ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return false;
        }

        if (getOffsetTimer() % 20 == 0) {
            removedLastSecond = 0;

            for (Direction dir : GTUtil.DIRECTIONS) {
                BlockPos offset = getPos().relative(dir);
                if (GTCapabilityHelper.getHazardContainer(getLevel(), offset, dir.getOpposite()) != null) {
                    if (getLevel().getBlockEntity(offset) instanceof DuctPipeBlockEntity duct &&
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
                            GTNetwork.sendToAllPlayersTrackingChunk(chunk, new SPacketRemoveHazardZone(chunkPos));
                        }
                        return null;
                    } else return zone;
                });
            }
        }
        return true;
    }
}
