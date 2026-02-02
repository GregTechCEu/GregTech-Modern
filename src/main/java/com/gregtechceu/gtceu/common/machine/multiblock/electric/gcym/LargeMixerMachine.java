package com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IFluidRenderMulti;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.syncsystem.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeMixerMachine extends WorkableElectricMultiblockMachine implements IFluidRenderMulti {

    @Getter
    @SyncToClient
    @RerenderOnChanged
    private @NotNull Set<BlockPos> fluidBlockOffsets = new HashSet<>();

    public LargeMixerMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    public void setFluidBlockOffsets(Set<BlockPos> offsets) {
        fluidBlockOffsets = offsets;
        syncDataHolder.markClientSyncFieldDirty("fluidBlockOffsets");
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        IFluidRenderMulti.super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        IFluidRenderMulti.super.onStructureInvalid();
    }

    @Override
    public @NotNull Set<BlockPos> saveOffsets() {
        Direction up = RelativeDirection.UP.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped());
        Direction back = getFrontFacing().getOpposite();
        Direction clockWise = RelativeDirection.RIGHT.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped());
        Direction counterClockWise = RelativeDirection.LEFT.getRelative(getFrontFacing(), getUpwardsFacing(),
                isFlipped());

        BlockPos pos = getBlockPos();
        BlockPos center = pos.relative(up, 3);

        Set<BlockPos> offsets = new HashSet<>();

        for (int i = 0; i < 3; i++) {
            center = center.relative(back);
            if (i % 2 == 0) {
                offsets.add(center.subtract(pos));
            }
            offsets.add(center.relative(clockWise).subtract(pos));
            offsets.add(center.relative(counterClockWise).subtract(pos));
        }
        return offsets;
    }
}
