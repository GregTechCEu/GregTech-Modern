package com.gregtechceu.gtceu.common.recipe;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ResearchData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ResearchCondition extends RecipeCondition {

    public final static ResearchCondition INSTANCE = new ResearchCondition();
    public ResearchData data;

    public ResearchCondition() {
        this.data = new ResearchData();
    }

    @Override
    public String getType() {
        return "reseach";
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

    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        this.data.toNetwork(buf);
    }

    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
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
