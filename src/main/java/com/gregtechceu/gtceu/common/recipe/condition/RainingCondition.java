package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class RainingCondition extends RecipeCondition<RainingCondition> {

    // spotless:off
    public static final Codec<RainingCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            Codec.FLOAT.fieldOf("level").forGetter(val -> val.level)
    ).apply(instance, RainingCondition::new));
    // spotless:on

    private float level;

    public RainingCondition(boolean isReverse, float level) {
        super(isReverse);
        this.level = level;
    }

    public RainingCondition(float level) {
        this.level = level;
    }

    @Override
    public RecipeConditionType<RainingCondition> getType() {
        return GTRecipeConditions.RAINING;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.rain.tooltip", level);
    }

    public float getLevel() {
        return level;
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.getLevel();
        return level.getRainLevel(1) >= this.level;
    }

    @Override
    public RainingCondition createTemplate() {
        return new RainingCondition();
    }
}
