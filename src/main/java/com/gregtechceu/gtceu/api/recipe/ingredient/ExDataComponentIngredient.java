package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.tag.GTIngredientTypes;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data component ingredient that clears the item cache when tags are reloaded.
 *
 * @see DataComponentIngredient DataComponentIngredient, the parent class
 * @see ExDataComponentFluidIngredient ExDataComponentFluidIngredient, its fluid equivalent
 */
public class ExDataComponentIngredient extends DataComponentIngredient {

    // spotless:off
    public static final MapCodec<ExDataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").forGetter(ExDataComponentIngredient::items),
            DataComponentPredicate.CODEC.fieldOf("components").forGetter(ExDataComponentIngredient::components),
            Codec.BOOL.optionalFieldOf("strict", false).forGetter(ExDataComponentIngredient::isStrict)
    ).apply(builder, ExDataComponentIngredient::new));
    // spotless:on

    private final @NotNull List<ItemStack> stacks;

    public ExDataComponentIngredient(HolderSet<Item> items, DataComponentPredicate components, boolean strict) {
        super(items, components, strict);
        this.stacks = items.stream()
                .map(i -> new ItemStack(i, 1, components.asPatch()))
                .collect(Collectors.toCollection(ArrayList::new));
        items.addInvalidationListener(this.stacks::clear);
    }

    private List<ItemStack> regenerateStacksIfEmpty() {
        if (this.stacks.isEmpty()) {
            this.items().stream()
                    .map(i -> new ItemStack(i, 1, components().asPatch()))
                    .forEach(this.stacks::add);
        }
        return this.stacks;
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (this.isStrict()) {
            for (ItemStack stack2 : this.regenerateStacksIfEmpty()) {
                if (ItemStack.isSameItemSameComponents(stack, stack2)) return true;
            }
            return false;
        } else {
            return this.items().contains(stack.getItemHolder()) && this.components().test(stack);
        }
    }

    @Override
    public @NotNull Stream<ItemStack> getItems() {
        return this.regenerateStacksIfEmpty().stream();
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return GTIngredientTypes.DATA_COMPONENT_INGREDIENT.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExDataComponentIngredient)) return false;
        return super.equals(obj);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static @NotNull Ingredient of(boolean strict, DataComponentMap map, HolderSet<Item> items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static @NotNull Ingredient of(boolean strict, DataComponentPredicate predicate, HolderSet<Item> items) {
        return new ExDataComponentIngredient(items, predicate, strict).toVanilla();
    }
}
