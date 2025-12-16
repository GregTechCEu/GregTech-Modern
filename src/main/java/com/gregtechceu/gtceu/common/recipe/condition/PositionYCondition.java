package com.gregtechceu.gtceu.common.recipe.condition;

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
public class PositionYCondition extends RecipeCondition {

    public static final Codec<PositionYCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition
            .isReverse(instance)
            .and(instance.group(
                    Codec.INT.fieldOf("min").forGetter(val -> val.min),
                    Codec.INT.fieldOf("max").forGetter(val -> val.max)))
            .apply(instance, PositionYCondition::new));

    public final static PositionYCondition INSTANCE = new PositionYCondition();
    private int min;
    private int max;

    public PositionYCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public PositionYCondition(boolean isReverse, int min, int max) {
        super(isReverse);
        this.min = min;
        this.max = max;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.POSITION_Y;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.pos_y.tooltip", this.min, this.max);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        int y = recipeLogic.machine.self().getPos().getY();
        return y >= this.min && y <= this.max;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new PositionYCondition();
    }
}
