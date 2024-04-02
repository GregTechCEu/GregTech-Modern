package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTIngredientTypes;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.mojang.serialization.Codec;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;

public class IntCircuitIngredient extends Ingredient {
    public static final ResourceLocation TYPE = GTCEu.id("circuit");

    public static final int CIRCUIT_MIN = 0;
    public static final int CIRCUIT_MAX = 32;
    public static final Codec<IntCircuitIngredient> CODEC = ExtraCodecs.intRange(CIRCUIT_MIN, CIRCUIT_MAX).xmap(IntCircuitIngredient::new, IntCircuitIngredient::getConfiguration);

    private static final IntCircuitIngredient[] INGREDIENTS = new IntCircuitIngredient[CIRCUIT_MAX + 1];

    public static IntCircuitIngredient circuitInput(int configuration) {
        if (configuration < CIRCUIT_MIN || configuration > CIRCUIT_MAX) {
            throw new IndexOutOfBoundsException("Circuit configuration " + configuration + " is out of range");
        }
        IntCircuitIngredient ingredient = INGREDIENTS[configuration];
        if (ingredient == null) {
            INGREDIENTS[configuration] = ingredient = new IntCircuitIngredient(configuration);
        }
        return ingredient;
    }

    @Getter
    private final int configuration;
    private ItemStack[] stacks;

    protected IntCircuitIngredient(int configuration) {
        super(Stream.of(new Ingredient.ItemValue(IntCircuitBehaviour.stack(configuration))), GTIngredientTypes.INT_CIRCUIT_INGREDIENT);
        this.configuration = configuration;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        return stack.is(GTItems.INTEGRATED_CIRCUIT.get()) && IntCircuitBehaviour.getCircuitConfiguration(stack) == this.configuration;
    }

    public IntCircuitIngredient copy() {
        return new IntCircuitIngredient(this.configuration);
    }
}
