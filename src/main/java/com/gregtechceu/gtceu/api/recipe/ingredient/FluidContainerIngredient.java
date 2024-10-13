package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.data.tag.GTIngredientTypes;
import com.gregtechceu.gtceu.utils.InfiniteFluidTransfer;

import com.lowdragmc.lowdraglib.side.fluid.*;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class FluidContainerIngredient implements ICustomIngredient {

    public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    SizedFluidIngredient.NESTED_CODEC.fieldOf("fluid").forGetter(FluidContainerIngredient::getFluid))
            .apply(instance, FluidContainerIngredient::new));

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

    @Nonnull
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
        IFluidHandler transfer = FluidTransferHelper.getFluidTransfer(stack);
        return transfer != null && this.extractFrom(transfer, true);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return GTIngredientTypes.FLUID_CONTAINER_INGREDIENT.get();
    }

    public ItemStack getExtractedStack(ItemStack input) {
        FluidActionResult result = FluidTransferHelper.tryEmptyContainer(input,
                new InfiniteFluidTransfer(1),
                (int) this.fluid.amount(),
                CommonHooks.getCraftingPlayer(),
                true);
        if (result.success) {
            return result.result;
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
