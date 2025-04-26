package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.data.tag.GTIngredientTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.VoidFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.MapCodec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public class FluidContainerIngredient implements ICustomIngredient {

    // spotless:off
    public static final MapCodec<FluidContainerIngredient> CODEC = SizedFluidIngredient.NESTED_CODEC.fieldOf("fluid")
            .xmap(FluidContainerIngredient::new, FluidContainerIngredient::getFluid);
    // spotless:on
    @Getter
    private final SizedFluidIngredient fluid;

    public FluidContainerIngredient(SizedFluidIngredient fluid) {
        this.fluid = fluid;
    }

    public FluidContainerIngredient(FluidStack fluidStack) {
        this(SizedFluidIngredient.of(
                TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).getPath()),
                fluidStack.getAmount()));
    }

    public FluidContainerIngredient(TagKey<Fluid> tag, int amount) {
        this(SizedFluidIngredient.of(tag, amount));
    }

    private Stream<ItemStack> cachedStacks;

    @NotNull
    @Override
    public Stream<ItemStack> getItems() {
        if (cachedStacks == null)
            cachedStacks = Arrays.stream(this.fluid.getFluids())
                    .map(stack -> stack.getFluid().getBucket().getDefaultInstance())
                    .filter(s -> !s.isEmpty());
        return this.cachedStacks;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(stack).orElse(null);
        return handler != null && this.extractFrom(handler, true);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return GTIngredientTypes.FLUID_CONTAINER_INGREDIENT.get();
    }

    public ItemStack getExtractedStack(ItemStack input) {
        FluidActionResult result = FluidUtil.tryEmptyContainer(input,
                VoidFluidHandler.INSTANCE,
                fluid.amount(),
                CommonHooks.getCraftingPlayer(),
                true);
        if (result.isSuccess()) {
            return result.getResult();
        }
        return input;
    }

    public boolean extractFrom(IFluidHandler handler, boolean simulate) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack inTank = handler.getFluidInTank(tank);
            if (fluid.test(inTank)) {
                FluidStack toExtract = inTank.copyWithAmount(fluid.amount());
                FluidStack extractedSim = handler.drain(toExtract, IFluidHandler.FluidAction.SIMULATE);
                if (extractedSim.getAmount() >= fluid.amount()) {
                    if (!simulate)
                        handler.drain(toExtract, IFluidHandler.FluidAction.EXECUTE);
                    return true;
                }
            }
        }
        return false;
    }
}
