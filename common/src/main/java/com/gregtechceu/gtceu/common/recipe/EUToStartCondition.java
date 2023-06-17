package com.gregtechceu.gtceu.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * @author Screret
 * @date 2023/6/16
 * @implNote EUToStartCondition
 */
@NoArgsConstructor
public class EUToStartCondition extends RecipeCondition {
    public static final EUToStartCondition INSTANCE = new EUToStartCondition();

    private long euToStart;

    public EUToStartCondition(long euToStart) {
        this.euToStart = euToStart;
    }

    @Override
    public String getType() {
        return "eu_to_start";
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.eu_to_start.tooltip");
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return recipeLogic.getMachine().getTraits().stream().filter(IEnergyContainer.class::isInstance)
                .anyMatch(energyContainer -> ((IEnergyContainer) energyContainer).getEnergyCapacity() > euToStart);
    }

    @Override
    public RecipeCondition createTemplate() {
        return new EUToStartCondition();
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("euToStart", euToStart);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
        super.deserialize(config);
        euToStart = GsonHelper.getAsLong(config, "euToStart", 0);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        euToStart = buf.readLong();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeLong(euToStart);
    }
}
