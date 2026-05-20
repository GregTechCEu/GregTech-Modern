package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class BiomeCondition extends RecipeCondition<BiomeCondition> {

    // spotless:off
    public static final Codec<BiomeCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(val -> val.biome)
    ).apply(instance, BiomeCondition::new));
    // spotless:on

    @Getter
    private ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, new ResourceLocation("dummy"));

    public BiomeCondition(boolean isReverse, ResourceKey<Biome> biome) {
        super(isReverse);
        this.biome = biome;
    }

    public BiomeCondition(ResourceKey<Biome> biome) {
        this.biome = biome;
    }

    @Override
    public RecipeConditionType<BiomeCondition> getType() {
        return GTRecipeConditions.BIOME;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.biome.tooltip",
                Component.translatableWithFallback(biome.location().toLanguageKey("biome"),
                        biome.location().toString()));
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.getLevel();
        Holder<Biome> biome = level.getBiome(recipeLogic.getBlockPos());
        return biome.is(this.biome);
    }

    @Override
    public BiomeCondition createTemplate() {
        return new BiomeCondition();
    }
}
