package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.Codec;
import lombok.experimental.ExtensionMethod;

import java.util.stream.Stream;

@ExtensionMethod(SizedIngredientExtensions.class)
public class SerializerFluidIngredient implements IContentSerializer<SizedFluidIngredient> {

    public static final FluidIngredient EMPTY_FLUID_INGREDIENT = new EmptyFluidIngredient();
    public static final SizedFluidIngredient EMPTY = new SizedFluidIngredient(EMPTY_FLUID_INGREDIENT, 1);

    public static SerializerFluidIngredient INSTANCE = new SerializerFluidIngredient();

    private SerializerFluidIngredient() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, SizedFluidIngredient content) {
        SizedFluidIngredient.STREAM_CODEC.encode(buf, content);
    }

    @Override
    public SizedFluidIngredient fromNetwork(RegistryFriendlyByteBuf buf) {
        return SizedFluidIngredient.STREAM_CODEC.decode(buf);
    }

    @Override
    public SizedFluidIngredient of(Object o) {
        if (o instanceof SizedFluidIngredient ingredient) {
            return ingredient.copy();
        }
        if (o instanceof FluidStack stack) {
            return RecipeHelper.makeSizedFluidIngredient(stack.copy());
        }
        throw new IllegalArgumentException("Unsupported fluid ingredient content: " + o);
    }

    @Override
    public SizedFluidIngredient defaultValue() {
        return EMPTY;
    }

    @Override
    public Class<SizedFluidIngredient> contentClass() {
        return SizedFluidIngredient.class;
    }

    @Override
    public Codec<SizedFluidIngredient> codec() {
        return SizedFluidIngredient.CODEC;
    }

    private static final class EmptyFluidIngredient extends FluidIngredient {

        @Override
        public boolean test(FluidStack stack) {
            return false;
        }

        @Override
        protected Stream<Holder<Fluid>> generateFluids() {
            return Stream.empty();
        }

        @Override
        public SlotDisplay display() {
            return SlotDisplay.Empty.INSTANCE;
        }

        @Override
        public boolean isSimple() {
            return true;
        }

        @Override
        public FluidIngredientType<?> getType() {
            throw new UnsupportedOperationException("GTCEu's empty fluid ingredient sentinel is not serializable");
        }

        @Override
        public int hashCode() {
            return EmptyFluidIngredient.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof EmptyFluidIngredient;
        }
    }
}
