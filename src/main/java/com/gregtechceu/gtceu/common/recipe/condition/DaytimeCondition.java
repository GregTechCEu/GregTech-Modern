package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
public class DaytimeCondition extends RecipeCondition<DaytimeCondition> {

    // spotless:off
    public static final MapCodec<DaytimeCondition> CODEC = RecipeCondition.simpleCodec(DaytimeCondition::new);
    // spotless:off

    public DaytimeCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<DaytimeCondition> getType() {
        return GTRecipeConditions.DAYTIME;
    }

    @Override
    public Component getTooltips() {
        if (isReverse) {
            return Component.translatable("recipe.condition.daytime.night.tooltip");
        } else {
            return Component.translatable("recipe.condition.daytime.day.tooltip");
        }
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && !level.isNight();
    }

    @Override
    public DaytimeCondition createTemplate() {
        return new DaytimeCondition();
    }
}
