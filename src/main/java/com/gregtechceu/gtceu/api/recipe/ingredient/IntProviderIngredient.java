package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import com.gregtechceu.gtceu.data.tag.GTIngredientTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import lombok.Getter;
import lombok.Setter;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public class IntProviderIngredient implements ICustomIngredient {

    public static final ResourceLocation TYPE = GTCEu.id("int_provider");
    public static final MapCodec<IntProviderIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("inner").forGetter(IntProviderIngredient::getInner),
            IntProvider.CODEC.fieldOf("count_provider").forGetter(IntProviderIngredient::getCountProvider)
    ).apply(instance, IntProviderIngredient::new));

    @Getter
    protected final IntProvider countProvider;
    @Setter
    protected Integer sampledCount = null;
    @Getter
    protected final Ingredient inner;
    @Setter
    protected Stream<ItemStack> itemStacks = null;

    public IntProviderIngredient(Ingredient inner, IntProvider countProvider) {
        this.inner = inner;
        this.countProvider = countProvider;
    }

    public IntProviderIngredient(@NotNull TagKey<Item> tag, IntProvider amount) {
        this(Ingredient.of(tag), amount);
    }

    public static IntProviderIngredient create(Ingredient inner, IntProvider countProvider) {
        return new IntProviderIngredient(inner, countProvider);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return inner.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        if (itemStacks == null)
            itemStacks = Arrays.stream(inner.getItems())
                    .map(i -> i.copyWithCount(getSampledCount(GTValues.RNG)));
        return itemStacks;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return GTIngredientTypes.INT_PROVIDER_INGREDIENT.get();
    }

    public int getSampledCount(@NotNull RandomSource random) {
        if (sampledCount == null) {
            sampledCount = countProvider.sample(random);
        }
        return sampledCount;
    }
}
