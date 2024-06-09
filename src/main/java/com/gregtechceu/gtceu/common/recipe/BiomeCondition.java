package com.gregtechceu.gtceu.common.recipe;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

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
public class BiomeCondition extends RecipeCondition {

    public static final MapCodec<BiomeCondition> CODEC = RecordCodecBuilder
            .mapCodec(instance -> RecipeCondition.isReverse(instance)
                    .and(ResourceLocation.CODEC.fieldOf("biome").forGetter(val -> val.biome))
                    .apply(instance, BiomeCondition::new));

    public final static BiomeCondition INSTANCE = new BiomeCondition();
    private ResourceLocation biome = ResourceLocation.parse("dummy");

    public BiomeCondition(boolean isReverse, ResourceLocation biome) {
        super(isReverse);
        this.biome = biome;
    }

    public BiomeCondition(ResourceLocation biome) {
        this.biome = biome;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.BIOME;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.biome.tooltip",
                LocalizationUtils.format("biome.%s.%s", biome.getNamespace(), biome.getPath()));
    }

    public ResourceLocation getBiome() {
        return biome;
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        if (level == null) return false;
        Holder<Biome> biome = level.getBiome(recipeLogic.machine.self().getPos());
        return biome.is(this.biome);
    }

    @Override
    public RecipeCondition createTemplate() {
        return new BiomeCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("biome", biome.toString());
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        biome = ResourceLocation.parse(
                GsonHelper.getAsString(config, "biome", "dummy"));
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(RegistryFriendlyByteBuf buf) {
        super.fromNetwork(buf);
        biome = ResourceLocation.parse(buf.readUtf());
        return this;
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeUtf(biome.toString());
    }
}
