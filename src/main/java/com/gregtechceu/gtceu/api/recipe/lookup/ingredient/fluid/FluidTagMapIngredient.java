package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidTagMapIngredient extends AbstractMapIngredient {

    protected TagKey<Fluid> tag;

    public FluidTagMapIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidIngredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>();
        for (FluidIngredient.Value value : ingredient.values) {
            if (value instanceof FluidIngredient.TagValue tagValue) {
                ingredients.add(new FluidTagMapIngredient(tagValue.tag()));
            }
        }
        return ingredients;
    }

    @SuppressWarnings("deprecation")
    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidStack stack) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>();
        stack.getFluid().builtInRegistryHolder().tags()
                .forEach(tag -> ingredients.add(new FluidTagMapIngredient(tag)));
        return ingredients;
    }

    @Override
    protected int hash() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return tag == ((FluidTagMapIngredient) obj).tag;
        }
        return false;
    }

    @Override
    public String toString() {
        return "FluidTagMapIngredient{" + "tag=" + tag.location() + "}";
    }
}
