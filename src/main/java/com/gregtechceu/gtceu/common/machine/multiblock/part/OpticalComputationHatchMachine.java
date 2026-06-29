package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpticalComputationHatchMachine extends MultiblockPartMachine {

    @Getter
    private final boolean transmitter;

    protected NotifiableComputationContainer computationContainer;

    public OpticalComputationHatchMachine(BlockEntityCreationInfo info, boolean transmitter) {
        super(info);
        this.transmitter = transmitter;
        this.computationContainer = attachTrait(new NotifiableComputationContainer(IO.IN, transmitter));
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
        return false;
    }
}
