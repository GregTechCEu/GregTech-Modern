package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class BiomeTagCondition extends RecipeCondition<BiomeTagCondition> {

    public static final Codec<BiomeTagCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(TagKey.codec(Registries.BIOME).fieldOf("biome_tag").forGetter(val -> val.biome))
                    .apply(instance, BiomeTagCondition::new));

    public final static BiomeTagCondition INSTANCE = new BiomeTagCondition();
    @Getter
    private TagKey<Biome> biome = TagKey.create(Registries.BIOME, new ResourceLocation("dummy"));

    public BiomeTagCondition(boolean isReverse, TagKey<Biome> biome) {
        super(isReverse);
        this.biome = biome;
    }

    public BiomeTagCondition(TagKey<Biome> biome) {
        this.biome = biome;
    }

    @Override
    public RecipeConditionType<BiomeTagCondition> getType() {
        return GTRecipeConditions.BIOME_TAG;
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
        Level level = recipeLogic.machine.self().getLevel();
        if (level == null) return false;
        Holder<Biome> biome = level.getBiome(recipeLogic.machine.self().getBlockPos());
        return biome.is(this.biome);
    }

    @Override
    public BiomeTagCondition createTemplate() {
        return new BiomeTagCondition();
    }
}
