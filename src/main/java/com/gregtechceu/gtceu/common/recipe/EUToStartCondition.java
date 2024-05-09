package com.gregtechceu.gtceu.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machines.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipes.GTRecipe;
import com.gregtechceu.gtceu.api.recipes.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipes.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

/**
 * @author Screret
 * @date 2023/6/16
 * @implNote EUToStartCondition
 */
@NoArgsConstructor
public class EUToStartCondition extends RecipeCondition {
    public static final MapCodec<EUToStartCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance)
        .and(Codec.LONG.fieldOf("eu_to_start").forGetter(val -> val.euToStart))
        .apply(instance, EUToStartCondition::new));
    public static final EUToStartCondition INSTANCE = new EUToStartCondition();

    private long euToStart;

    public EUToStartCondition(long euToStart) {
        this.euToStart = euToStart;
    }

    public EUToStartCondition(boolean isReverse, long euToStart) {
        super(isReverse);
        this.euToStart = euToStart;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.EU_TO_START;
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

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("euToStart", euToStart);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        euToStart = GsonHelper.getAsLong(config, "euToStart", 0);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(RegistryFriendlyByteBuf buf) {
        super.fromNetwork(buf);
        euToStart = buf.readLong();
        return this;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeLong(euToStart);
    }
}
