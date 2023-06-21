package com.gregtechceu.gtceu.common.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote SteamVentCondition
 */
@NoArgsConstructor
public class VentCondition extends RecipeCondition {
    public final static VentCondition INSTANCE = new VentCondition();
    @Override
    public String getType() {
        return "steam_vent";
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.steam_vent.tooltip");
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (recipeLogic.getProgress() % 10 == 0 && recipeLogic.machine instanceof IExhaustVentMachine ventMachine) {
            return !(ventMachine.needsVenting() && ventMachine.isVentingBlocked());
        }
        return true;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new VentCondition();
    }
}