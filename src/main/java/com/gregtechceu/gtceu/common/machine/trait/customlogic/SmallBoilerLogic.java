package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.machine.trait.customlogic.SteamBoilerLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import static com.gregtechceu.gtceu.data.recipe.GTRecipeTypes.STEAM_BOILER_RECIPES;

public class SmallBoilerLogic extends SteamBoilerLogic {

    public static final SmallBoilerLogic INSTANCE = new SmallBoilerLogic();

    private SmallBoilerLogic() {}

    @Override
    protected GTRecipeType getRecipeType() {
        return STEAM_BOILER_RECIPES;
    }

    @Override
    protected int modifyBurnTime(int originalBurnTime) {
        return originalBurnTime * 12;
    }
}
