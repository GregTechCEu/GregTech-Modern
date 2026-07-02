package com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.multiblock.MultiblockFluidRendererTrait;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeChemicalBathMachine extends WorkableElectricMultiblockMachine {

    @Getter
    @SyncToClient
    @RerenderOnChanged
    private final MultiblockFluidRendererTrait fluidRendererTrait;

    public LargeChemicalBathMachine(BlockEntityCreationInfo info) {
        super(info);
        fluidRendererTrait = attachTrait(new MultiblockFluidRendererTrait(this::saveOffsets));
    }

    public Set<BlockPos> saveOffsets() {
        Direction up = RelativeDirection.UP.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
        Direction back = getFrontFacing().getOpposite();
        Direction clockWise = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());
        Direction counterClockWise = RelativeDirection.LEFT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());

        BlockPos pos = getBlockPos();
        BlockPos center = pos.relative(up);

        Set<BlockPos> offsets = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            center = center.relative(back);
            offsets.add(center.subtract(pos));
            offsets.add(center.relative(clockWise).subtract(pos));
            offsets.add(center.relative(counterClockWise).subtract(pos));
        }
        return offsets;
    }
}
