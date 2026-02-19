package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class ThunderCondition extends RecipeCondition<ThunderCondition> {

    // spotless:off
    public static final MapCodec<ThunderCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance).and(
            Codec.FLOAT.fieldOf("level").forGetter(ThunderCondition::getLevel)
    ).apply(instance, ThunderCondition::new));
    // spotless:on

    @Getter
    private float level;

    public ThunderCondition(boolean isReverse, float level) {
        super(isReverse);
        this.level = level;
    }

    public ThunderCondition(float level) {
        this.level = level;
    }

    @Override
    public RecipeConditionType<ThunderCondition> getType() {
        return GTRecipeConditions.THUNDER;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.thunder.tooltip", level);
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && level.getThunderLevel(1) >= this.level;
    }

    @Override
    public ThunderCondition createTemplate() {
        return new ThunderCondition();
    }
}
