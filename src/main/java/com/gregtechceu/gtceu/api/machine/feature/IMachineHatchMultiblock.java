package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

public interface IMachineHatchMultiblock extends IMachineFeature {

    /**
     * @return a String array of blacklisted RecipeMaps for the
     *         {@link com.gregtechceu.gtceu.common.machine.multiblock.part.MachineHatchPartMachine}
     */
    default GTRecipeType[] getBlacklist() {
        return new GTRecipeType[0];
    }

    default int getMachineLimit() {
        return 64;
    }

    void notifyMachineChanged();
}