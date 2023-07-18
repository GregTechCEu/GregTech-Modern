package com.gregtechceu.gtceu.common.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.NoArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote DimensionCondition, specific dimension
 */
@NoArgsConstructor
public class BiomeCondition extends RecipeCondition {

    public final static BiomeCondition INSTANCE = new BiomeCondition();
    private ResourceLocation biome = new ResourceLocation("dummy");

    public BiomeCondition(ResourceLocation biome) {
        this.biome = biome;
    }

    @Override
    public String getType() {
        return "biome";
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.biome.tooltip", LocalizationUtils.format("biome.%s.%s", biome.getNamespace(), biome.getPath()));
    }

    public ResourceLocation getBiome() {
        return biome;
    }

    @Override
    public boolean test(@Nonnull GTRecipe recipe, @Nonnull RecipeLogic recipeLogic) {
        Level level = recipeLogic.machine.self().getLevel();
        if (level == null) return false;
        Holder<Biome> biome = level.getBiome(recipeLogic.machine.self().getPos());
        return biome.is(this.biome);
    }

    @Override
    public RecipeCondition createTemplate() {
        return new BiomeCondition();
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("biome", biome.toString());
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
        super.deserialize(config);
        biome = new ResourceLocation(
                GsonHelper.getAsString(config, "biome", "dummy"));
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        biome = new ResourceLocation(buf.readUtf());
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeUtf(biome.toString());
    }

}
