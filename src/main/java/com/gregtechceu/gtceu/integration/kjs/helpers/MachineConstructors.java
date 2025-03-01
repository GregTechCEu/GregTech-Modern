package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeCombustionEngineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;

/**
 * Collection of functions that can be used in the Machine Creation Functions in KJS definitions
 * Makes using them for KJS easier, as loading the relevant class won't be required.
 */
@SuppressWarnings("unused")
public final class MachineConstructors {

    // This one in particular stops a crash when trying to define a new LCE
    // The crash is caused by the static FluidStack members in LargeCombustionEngine.class
    public static MultiblockControllerMachine createLargeCombustionEngine(IMachineBlockEntity holder, int tier) {
        return new LargeCombustionEngineMachine(holder, tier);
    }

    public static MultiblockControllerMachine createLargeTurbine(IMachineBlockEntity holder, int tier) {
        return new LargeTurbineMachine(holder, tier);
    }

    public static MultiblockControllerMachine createFusionReactor(IMachineBlockEntity holder, int tier) {
        return new FusionReactorMachine(holder, tier);
    }

    public static MultiblockControllerMachine createSteamMultiblock(IMachineBlockEntity holder, int parallels) {
        return new SteamParallelMultiblockMachine(holder, parallels);
    }
}
