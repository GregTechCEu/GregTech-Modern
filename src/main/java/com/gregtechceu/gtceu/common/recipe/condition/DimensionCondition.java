package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class DimensionCondition extends RecipeCondition<DimensionCondition> {

    // spotless:off
    public static final MapCodec<DimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance).and(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionCondition::getDimension)
    ).apply(instance, DimensionCondition::new));
    // spotless:on

    @Getter
    private ResourceKey<Level> dimension;

    public DimensionCondition(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public DimensionCondition(boolean isReverse, ResourceKey<Level> dimension) {
        super(isReverse);
        this.dimension = dimension;
    }

    @Override
    public RecipeConditionType<DimensionCondition> getType() {
        return GTRecipeConditions.DIMENSION;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        return Component.translatableEscape("recipe.condition.dimension.tooltip", getDimensionName(this.dimension));
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && dimension == level.dimension();
    }

    @Override
    public DimensionCondition createTemplate() {
        return new DimensionCondition();
    }

    public static Component getDimensionName(ResourceKey<Level> dimension) {
        return getDimensionName(dimension.identifier());
    }

    public static Component getDimensionName(Identifier dimension) {
        return Component.translatableWithFallback(dimension.toLanguageKey(Level.TRANSLATION_PREFIX),
                dimension.toString());
    }
}
