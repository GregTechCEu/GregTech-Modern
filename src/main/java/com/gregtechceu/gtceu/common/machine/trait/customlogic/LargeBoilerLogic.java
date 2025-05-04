package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.machine.trait.customlogic.SteamBoilerLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import static com.gregtechceu.gtceu.data.recipe.GTRecipeTypes.LARGE_BOILER_RECIPES;

public class LargeBoilerLogic extends SteamBoilerLogic {

    public static final LargeBoilerLogic INSTANCE = new LargeBoilerLogic();

    private LargeBoilerLogic() {}

    @Override
    protected GTRecipeType getRecipeType() {
        return LARGE_BOILER_RECIPES;
    }

    @Override
    protected int modifyBurnTime(int originalBurnTime) {
        return originalBurnTime / 80;
    }
}
