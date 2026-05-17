package com.gregtechceu.gtceu.common.machine.trait.hazard;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.common.blockentity.DuctPipeBlockEntity;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
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

import java.util.function.BiPredicate;

public class EnvironmentalHazardCleanerTrait extends MachineTrait {

    public static final MachineTraitType<EnvironmentalHazardCleanerTrait> TYPE = new MachineTraitType<>(
            EnvironmentalHazardCleanerTrait.class);

    @Getter
    protected float removedLastSecond;

    @Getter
    protected float amountPerOperation;
    @Getter
    protected MedicalCondition conditionToRemove;
    @Getter
    protected int cleaningRadius;
    @Getter
    private boolean cleaningOperationInProgress;

    private final @Nullable BiPredicate<MedicalCondition, Float> cleaningHandler;

    public EnvironmentalHazardCleanerTrait(int cleaningRadius,
                                           @Nullable BiPredicate<MedicalCondition, Float> validateCleaningOperation) {
        super();
        this.cleaningRadius = cleaningRadius;
        this.cleaningHandler = validateCleaningOperation;
    }

    @Override
    public MachineTraitType<EnvironmentalHazardCleanerTrait> getTraitType() {
        return TYPE;
    }

    public boolean cleanHazard(MedicalCondition condition, float totalAmountToRemove) {
        if (cleaningHandler == null) return beginCleaningOperation(condition, totalAmountToRemove);
        return cleaningHandler.test(condition, totalAmountToRemove);
    }

    public boolean beginCleaningOperation(MedicalCondition condition, float amountPerOperation) {
        if (cleaningOperationInProgress) return false;

        this.conditionToRemove = condition;
        this.amountPerOperation = amountPerOperation;
        this.cleaningOperationInProgress = true;
        return true;
    }

    public void endCleaningOperation() {
        this.cleaningOperationInProgress = false;
    }

    public void cleanHazard() {
        if (!cleaningOperationInProgress) return;
        if (getMachine().getOffsetTimer() % 20 == 0) {
            removedLastSecond = 0;

            for (Direction dir : GTUtil.DIRECTIONS) {
                BlockPos offset = getBlockPos().relative(dir);
                if (GTCapabilityHelper.getHazardContainer(getLevel(), offset, dir.getOpposite()) != null) {
                    if (getLevel().getBlockEntity(offset) instanceof DuctPipeBlockEntity duct &&
                            !duct.isConnected(dir.getOpposite())) {
                        continue;
                    }
                    return;
                }
            }

            final ServerLevel serverLevel = (ServerLevel) getLevel();
            EnvironmentalHazardSavedData savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);

            final ChunkPos pos = new ChunkPos(getBlockPos());
            Object2FloatMap<ChunkPos> relativePositions = new Object2FloatOpenHashMap<>();
            if (cleaningRadius <= 0) {
                // LV scrubber can only process the chunk it's in
                relativePositions.put(pos, 1);
            } else {
                for (int x = -cleaningRadius; x <= cleaningRadius; ++x) {
                    for (int z = -cleaningRadius; z <= cleaningRadius; ++z) {
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

                    float toClean = amountPerOperation / distance;
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
        return;
    }
}
