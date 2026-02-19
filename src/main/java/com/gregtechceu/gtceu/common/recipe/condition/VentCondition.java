package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.MapCodec;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class VentCondition extends RecipeCondition<VentCondition> {

    public static final MapCodec<VentCondition> CODEC = RecipeCondition.simpleCodec(VentCondition::new);
    public final static VentCondition INSTANCE = new VentCondition();

    public VentCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<VentCondition> getType() {
        return GTRecipeConditions.VENT;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.steam_vent.tooltip");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (recipeLogic.getProgress() % 10 == 0 && recipeLogic.machine instanceof IExhaustVentMachine ventMachine) {
            return !(ventMachine.isNeedsVenting() && ventMachine.isVentingBlocked());
        }
        return true;
    }

    @Override
    public VentCondition createTemplate() {
        return new VentCondition();
    }
}
