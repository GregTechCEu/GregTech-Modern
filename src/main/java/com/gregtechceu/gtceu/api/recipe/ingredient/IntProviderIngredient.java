package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient.intProviderEqual;

/**
 * Allows an {@link Ingredient} to be created with a ranged {@code count}, which will be randomly rolled upon recipe
 * start (input) / completion (output).
 * Instantiated using {@link IntProviderIngredient#of}, with a {@link Ingredient} or {@link ItemStack},
 * and an {@link IntProvider}.
 * Functions similarly to {@link IntProviderFluidIngredient}.
 */
public class IntProviderIngredient implements ICustomIngredient, IRangedIngredient {

    // spotless:off
    public static final MapCodec<IntProviderIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("inner").forGetter(IntProviderIngredient::getInner),
            IntProvider.CODEC.fieldOf("count_provider").forGetter(IntProviderIngredient::getCountProvider),
            Codec.INT.optionalFieldOf("sampled_count", -1).forGetter(IRangedIngredient::getSampledCount)
    ).apply(instance, IntProviderIngredient::new));
    // spotless:on
    public static final ResourceLocation TYPE = GTCEu.id("int_provider");
    public static final ItemStack[] EMPTY_STACK_ARRAY = new ItemStack[0];

    @Getter
    protected final IntProvider countProvider;
    /**
     * The last result of {@link IntProviderIngredient#rollSampledCount(RandomSource)}. -1 if not rolled.
     */
    @Getter
    @Setter
    protected int sampledCount = -1;
    /**
     * The {@link Ingredient} to have a ranged amount.
     */
    @Getter
    protected final Ingredient inner;
    @Setter
    protected ItemStack @Nullable [] itemStacks = null;

    protected IntProviderIngredient(Ingredient inner, IntProvider countProvider) {
        this.inner = inner;
        this.countProvider = countProvider;
    }

    protected IntProviderIngredient(Ingredient inner, IntProvider countProvider, int sampledCount) {
        this.inner = inner;
        this.countProvider = countProvider;
        this.sampledCount = sampledCount;
    }

    /**
     * @param inner         {@link Ingredient}
     * @param countProvider usually as {@link net.minecraft.util.valueproviders.UniformInt#of(int, int)}
     */
    public static IntProviderIngredient of(Ingredient inner, IntProvider countProvider) {
        Preconditions.checkArgument(countProvider.getMinValue() >= 0,
                "IntProviderIngredient must have a min value of at least 0.");
        return new IntProviderIngredient(inner, countProvider);
    }

    /**
     * @param stack         {@link ItemStack}
     * @param countProvider usually as {@link net.minecraft.util.valueproviders.UniformInt#of(int, int)}
     */
    public static IntProviderIngredient of(ItemStack stack, IntProvider countProvider) {
        Ingredient inner = Ingredient.of(stack);
        return of(inner, countProvider);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return inner.test(stack);
    }

    /**
     * Gets a usable {@link ItemStack ItemStack[]} from this {@link IntProviderIngredient}.
     * If this ingredient has not yet had its {@link IntProviderIngredient#sampledCount} rolled, rolls it.
     * 
     * @return a {@link ItemStack ItemStack[]} with count {@link IntProviderIngredient#sampledCount}
     */
    public ItemStack[] getItemStacks() {
        if (itemStacks == null) {
            int cachedCount = rollSampledCount();
            if (cachedCount == 0) {
                return EMPTY_STACK_ARRAY;
            }
            var innerStacks = inner.getItems();
            this.itemStacks = new ItemStack[innerStacks.length];
            for (int i = 0; i < itemStacks.length; i++) {
                itemStacks[i] = innerStacks[i].copyWithCount(cachedCount);
            }
        }
        return itemStacks;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(getItemStacks());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public int hashCode() {
        return this.inner.hashCode() * 31 * this.countProvider.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IntProviderIngredient other)) {
            return false;
        }

        return this.inner.equals(other.inner) && intProviderEqual(this.countProvider, other.countProvider);
    }

    @Override
    public IngredientType<IntProviderIngredient> getType() {
        return GTIngredientTypes.INT_PROVIDER_INGREDIENT.get();
    }

    /**
     * Gets a {@link ItemStack} containing the maximum possible output from this {@link IntProviderIngredient}.
     * Mainly used for things like Recipe provider simulations to see if there is enough inventory space to handle
     * the recipe output.
     * 
     * @return a {@link ItemStack} with count {@link IntProvider#getMaxValue()}
     */
    public @NotNull ItemStack getMaxSizeStack() {
        if (inner.getItems().length == 0) return ItemStack.EMPTY;
        else return inner.getItems()[0].copyWithCount(countProvider.getMaxValue());
    }

    /**
     * If this ingredient has not yet had its {@link IntProviderIngredient#sampledCount} rolled, rolls it and returns
     * the roll.
     * If it has, returns the existing roll.
     * 
     * @param random {@link RandomSource}, must be threadsafe, usually called using {@link GTValues#RNG}.
     * @return the count rolled
     */
    @Override
    public int rollSampledCount(@NotNull RandomSource random) {
        if (sampledCount == -1) {
            sampledCount = countProvider.sample(random);
        }
        return sampledCount;
    }

    /**
     * Resets the random roll on this ingredient
     */
    public void reset() {
        sampledCount = -1;
        itemStacks = null;
    }
}
