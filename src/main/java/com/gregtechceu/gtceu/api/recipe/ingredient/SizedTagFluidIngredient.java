package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;

import java.util.stream.Stream;

public class SizedTagFluidIngredient extends TagFluidIngredient {
    public static final MapCodec<SizedTagFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.FLUID).fieldOf("tag").forGetter(SizedTagFluidIngredient::tag),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("amount").forGetter(SizedTagFluidIngredient::getAmount)
    ).apply(instance, SizedTagFluidIngredient::new));

    @Getter @Setter
    private int amount;

    public SizedTagFluidIngredient(TagKey<Fluid> tag, int amount) {
        super(tag);
        this.amount = amount;
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return BuiltInRegistries.FLUID.getTag(tag())
                .stream()
                .flatMap(HolderSet::stream)
                .map(fluid -> new FluidStack(fluid, amount));
    }

    public SizedTagFluidIngredient copy() {
        return new SizedTagFluidIngredient(tag(), getAmount());
    }
}
