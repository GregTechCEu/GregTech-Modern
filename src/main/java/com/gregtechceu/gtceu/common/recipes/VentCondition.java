package com.gregtechceu.gtceu.common.recipes;

import com.gregtechceu.gtceu.api.machines.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machines.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipes.GTRecipe;
import com.gregtechceu.gtceu.api.recipes.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipes.condition.RecipeConditionType;
import com.gregtechceu.gtceu.data.recipes.GTRecipeConditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    public static final MapCodec<VentCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance)
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
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
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