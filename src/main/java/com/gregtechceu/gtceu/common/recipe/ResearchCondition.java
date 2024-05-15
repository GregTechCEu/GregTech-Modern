package com.gregtechceu.gtceu.common.recipe;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ResearchCondition extends RecipeCondition {

    public static final MapCodec<ResearchCondition> CODEC = RecordCodecBuilder
            .mapCodec(instance -> RecipeCondition.isReverse(instance)
                    .and(
                            ResearchData.CODEC.fieldOf("research").forGetter(val -> val.data))
                    .apply(instance, ResearchCondition::new));
    public static final ResearchCondition INSTANCE = new ResearchCondition();
    public ResearchData data;

    public ResearchCondition() {
        this.data = new ResearchData();
    }

    public ResearchCondition(boolean isReverse, ResearchData data) {
        super(isReverse);
        this.data = data;
    }

    @Override
    public RecipeConditionType<ResearchCondition> getType() {
        return GTRecipeConditions.RESEARCH;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtceu.recipe.research");
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject value = super.serialize();
        value.add("research", this.data.toJson());
        return value;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        this.data = ResearchData.fromJson(config.getAsJsonArray("research"));
        return this;
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
        super.toNetwork(buf);
        this.data.toNetwork(buf);
    }

    public RecipeCondition fromNetwork(RegistryFriendlyByteBuf buf) {
        super.fromNetwork(buf);
        this.data = ResearchData.fromNetwork(buf);
        return this;
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return true;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new ResearchCondition();
    }
}
