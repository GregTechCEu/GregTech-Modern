package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.recipe.EnvironmentalHazardCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

public class AirScrubberMachine extends SimpleTieredMachine {

    public static final float MIN_CLEANING_PER_OPERATION = 10;

    private final float cleaningPerOperation;

    public AirScrubberMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachines.defaultTankSizeFunction, args);
        this.cleaningPerOperation = MIN_CLEANING_PER_OPERATION * tier;
    }

    @NotNull
    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new AirScrubberLogic(this);
    }

    @Override
    public boolean dampingWhenWaiting() {
        return false;
    }

    @Override
    public boolean onWorking() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return super.onWorking();
        }

        if (getOffsetTimer() % 20 == 0) {
            final ServerLevel serverLevel = (ServerLevel) getLevel();
            EnvironmentalHazardSavedData savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);

            final ChunkPos pos = new ChunkPos(getPos());
            ChunkPos[] relativePositions = new ChunkPos[] {
                    pos,
                    new ChunkPos(pos.x, pos.z - 1),
                    new ChunkPos(pos.x, pos.z + 1),
                    new ChunkPos(pos.x - 1, pos.z),
                    new ChunkPos(pos.x + 1, pos.z)
            };
            for (ChunkPos rel : relativePositions) {
                savedData.getHazardZones().compute(rel, (chunkPos, v) -> {
                    if (v == null || v.strength() <= 0) {
                        return null;
                    }
                    EnvironmentalHazardSavedData.HazardZone zone;
                    if (chunkPos.equals(pos)) {
                        zone = new EnvironmentalHazardSavedData.HazardZone(
                                v.source(),
                                v.strength() - cleaningPerOperation * 2 * getTier(),
                                v.canSpread(),
                                v.trigger(),
                                v.condition());
                    } else {
                        zone = new EnvironmentalHazardSavedData.HazardZone(
                                v.source(),
                                v.strength() - cleaningPerOperation * getTier(),
                                v.canSpread(),
                                v.trigger(),
                                v.condition());
                    }
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
        return super.onWorking();
    }

    public static class AirScrubberLogic extends RecipeLogic {

        public AirScrubberLogic(AirScrubberMachine machine) {
            super(machine);
        }

        @Override
        public AirScrubberMachine getMachine() {
            return (AirScrubberMachine) super.getMachine();
        }

        @Override
        protected boolean handleRecipeIO(GTRecipe recipe, IO io) {
            Direction output = getMachine().getOutputFacingFluids();
            if (output == null) {
                return super.handleRecipeIO(recipe, io);
            }
            final IHazardParticleContainer container = GTCapabilityHelper.getHazardContainer(
                    getMachine().getLevel(), getMachine().getPos().relative(output), output.getOpposite());
            if (container != null) {
                // if we have a valid hazard container on the fluid output, then push the particles into it instead of
                // converting them into fluid/item form.
                MutableBoolean didFindCondition = new MutableBoolean(false);
                recipe.conditions
                        .stream()
                        .filter(EnvironmentalHazardCondition.class::isInstance)
                        .map(EnvironmentalHazardCondition.class::cast)
                        .findFirst()
                        .ifPresent(condition -> {
                            container.addHazard(condition.getCondition(),
                                    6 * getMachine().cleaningPerOperation * getMachine().getTier());
                            didFindCondition.setTrue();
                        });
                if (didFindCondition.getValue()) {
                    return true;
                }
            }
            return super.handleRecipeIO(recipe, io);
        }
    }
}
