package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class VentCondition extends RecipeCondition {

    public static final Codec<VentCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .apply(instance, VentCondition::new));
    public final static VentCondition INSTANCE = new VentCondition();

    public VentCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<?> getType() {
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
    public RecipeCondition createTemplate() {
        return new VentCondition();
    }
}
