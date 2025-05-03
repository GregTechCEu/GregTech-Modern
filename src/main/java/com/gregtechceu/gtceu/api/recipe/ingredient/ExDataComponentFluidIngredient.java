package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.tag.GTIngredientTypes;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data component fluid ingredient that clears the fluid cache when tags are reloaded.
 *
 * @see DataComponentFluidIngredient DataComponentFluidIngredient, the parent class
 * @see ExDataComponentIngredient ExDataComponentIngredient, its item equivalent
 */
public class ExDataComponentFluidIngredient extends DataComponentFluidIngredient {

    // spotless:off
    public static final MapCodec<ExDataComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            HolderSetCodec.create(Registries.FLUID, BuiltInRegistries.FLUID.holderByNameCodec(), false).fieldOf("fluids").forGetter(ExDataComponentFluidIngredient::fluids),
            DataComponentPredicate.CODEC.fieldOf("components").forGetter(ExDataComponentFluidIngredient::components),
            Codec.BOOL.optionalFieldOf("strict", false).forGetter(ExDataComponentFluidIngredient::isStrict)
    ).apply(builder, ExDataComponentFluidIngredient::new));
    // spotless:on

    private final @NotNull List<FluidStack> stacks;

    public ExDataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentPredicate components, boolean strict) {
        super(fluids, components, strict);
        this.stacks = fluids.stream()
                .map(i -> new FluidStack(i, FluidType.BUCKET_VOLUME, components.asPatch()))
                .collect(Collectors.toCollection(ArrayList::new));
        fluids.addInvalidationListener(this.stacks::clear);
    }

    private List<FluidStack> regenerateStacksIfEmpty() {
        if (this.stacks.isEmpty()) {
            this.fluids().stream()
                    .map(i -> new FluidStack(i, FluidType.BUCKET_VOLUME, components().asPatch()))
                    .forEach(this.stacks::add);
        }
        return this.stacks;
    }

    @Override
    public boolean test(@NotNull FluidStack stack) {
        if (this.isStrict()) {
            for (FluidStack stack2 : this.regenerateStacksIfEmpty()) {
                if (FluidStack.isSameFluidSameComponents(stack, stack2)) return true;
            }
            return false;
        } else {
            return this.fluids().contains(stack.getFluidHolder()) && this.components().test(stack);
        }
    }

    public @NotNull Stream<FluidStack> generateStacks() {
        return this.regenerateStacksIfEmpty().stream();
    }

    @Override
    public @NotNull FluidIngredientType<?> getType() {
        return GTIngredientTypes.DATA_COMPONENT_FLUID_INGREDIENT.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExDataComponentFluidIngredient)) return false;
        return super.equals(obj);
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static @NotNull FluidIngredient of(boolean strict, DataComponentMap map, TagKey<Fluid> tag) {
        return of(strict, map, BuiltInRegistries.FLUID.getOrCreateTag(tag));
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static @NotNull FluidIngredient of(boolean strict, DataComponentMap map, HolderSet<Fluid> fluids) {
        return of(strict, DataComponentPredicate.allOf(map), fluids);
    }

    /**
     * Creates a new ingredient matching any fluid from the list, containing the given components
     */
    public static @NotNull FluidIngredient of(boolean strict, DataComponentPredicate predicate,
                                              HolderSet<Fluid> fluids) {
        return new ExDataComponentFluidIngredient(fluids, predicate, strict);
    }
}
