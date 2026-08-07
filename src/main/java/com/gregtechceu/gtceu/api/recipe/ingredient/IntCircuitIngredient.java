package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.mojang.serialization.MapCodec;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class IntCircuitIngredient implements ICustomIngredient {

    public static final ResourceLocation ID = GTCEu.id("circuit");

    public static final int CIRCUIT_MIN = 0;
    public static final int CIRCUIT_MAX = IntCircuitBehaviour.CIRCUIT_MAX;
    // spotless:off
    public static final MapCodec<IntCircuitIngredient> CODEC = ExtraCodecs.intRange(CIRCUIT_MIN, CIRCUIT_MAX).fieldOf("configuration")
            .xmap(IntCircuitIngredient::getIngredient, IntCircuitIngredient::getConfiguration);
    // spotless:on
    private static final IntCircuitIngredient[] INGREDIENTS = new IntCircuitIngredient[CIRCUIT_MAX + 1];

    public static Ingredient circuit(int configuration) {
        return getIngredient(configuration).toVanilla();
    }

    private static IntCircuitIngredient getIngredient(int configuration) {
        if (configuration < CIRCUIT_MIN || configuration > CIRCUIT_MAX) {
            throw new IndexOutOfBoundsException("Circuit configuration " + configuration + " is out of range");
        }
        IntCircuitIngredient ingredient = INGREDIENTS[configuration];
        if (ingredient == null) {
            INGREDIENTS[configuration] = ingredient = new IntCircuitIngredient(configuration);
        }
        return ingredient;
    }

    @Getter(AccessLevel.PRIVATE)
    private final int configuration;
    private final ItemStack stack;

    protected IntCircuitIngredient(int configuration) {
        this.configuration = configuration;
        this.stack = IntCircuitBehaviour.stack(configuration);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        return stack.is(GTItems.PROGRAMMED_CIRCUIT.get()) &&
                IntCircuitBehaviour.getCircuitConfiguration(stack) == this.configuration;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(stack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return GTIngredientTypes.INT_CIRCUIT_INGREDIENT.get();
    }

    public DataComponentIngredient convertToData() {
        return (DataComponentIngredient) DataComponentIngredient.of(true, this.stack).getCustomIngredient();
    }
}
