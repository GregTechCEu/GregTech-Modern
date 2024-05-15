package com.gregtechceu.gtceu.common.recipe;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote DimensionCondition, specific dimension
 */
@NoArgsConstructor
public class DimensionCondition extends RecipeCondition {

    public static final MapCodec<DimensionCondition> CODEC = RecordCodecBuilder
            .mapCodec(instance -> RecipeCondition.isReverse(instance)
                    .and(ResourceLocation.CODEC.fieldOf("dimension").forGetter(val -> val.dimension))
                    .apply(instance, DimensionCondition::new));

    public final static DimensionCondition INSTANCE = new DimensionCondition();
    private ResourceLocation dimension = new ResourceLocation("dummy");

    public DimensionCondition(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public DimensionCondition(boolean isReverse, ResourceLocation dimension) {
        super(isReverse);
        this.dimension = dimension;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.DIMENSION;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.dimension.tooltip", dimension);
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        return level != null && dimension.equals(level.dimension().location());
    }

    @Override
    public RecipeCondition createTemplate() {
        return new DimensionCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("dim", dimension.toString());
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        dimension = new ResourceLocation(
                GsonHelper.getAsString(config, "dim", "dummy"));
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(RegistryFriendlyByteBuf buf) {
        super.fromNetwork(buf);
        dimension = new ResourceLocation(buf.readUtf());
        return this;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeUtf(dimension.toString());
    }
}
