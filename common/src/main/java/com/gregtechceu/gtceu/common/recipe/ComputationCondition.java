package com.gregtechceu.gtceu.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@NoArgsConstructor
public class ComputationCondition extends RecipeCondition {
    public static final ComputationCondition INSTANCE = new ComputationCondition();

    private int cwuPerTick;

    public ComputationCondition(int cwuPerTick) {
        this.cwuPerTick = cwuPerTick;
    }

    @Override
    public String getType() {
        return "cwu_per_tick";
    }

    @Override
    public Component getTooltips() {
        return null;
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (recipeLogic.getMachine() instanceof IOpticalComputationReceiver receiver) {
            return receiver.getComputationProvider().requestCWUt(this.cwuPerTick, true) >= this.cwuPerTick;
        }
        return true;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new ComputationCondition();
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("cwuPerTick", cwuPerTick);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
        super.deserialize(config);
        cwuPerTick = GsonHelper.getAsInt(config, "cwuPerTick", 0);
        return this;
    }
}
