package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.machine.trait.ExhaustVentMachineTrait;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class VentCondition extends RecipeCondition<VentCondition> {

    public static final Codec<VentCondition> CODEC = RecipeCondition.simpleCodec(VentCondition::new);
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
        var ventTrait = recipeLogic.getMachine().getTrait(ExhaustVentMachineTrait.TYPE);
        if (recipeLogic.getProgress() % 10 == 0 && ventTrait != null) {
            return !(ventTrait.isNeedsVenting() && ventTrait.isVentingBlocked());
        }
        return true;
    }

    @Override
    public VentCondition createTemplate() {
        return new VentCondition();
    }
}
