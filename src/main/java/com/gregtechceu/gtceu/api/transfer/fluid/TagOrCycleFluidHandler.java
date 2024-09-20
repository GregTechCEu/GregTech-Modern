package com.gregtechceu.gtceu.api.transfer.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagOrCycleFluidHandler implements IFluidHandlerModifiable {

    @Getter
    private List<Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>>> stacks;

    private List<List<FluidStack>> unwrapped = null;

    public TagOrCycleFluidHandler(List<Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>>> stacks) {
        updateStacks(stacks);
    }

    public void updateStacks(List<Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>>> stacks) {
        this.stacks = new ArrayList<>(stacks);
        this.unwrapped = null;
    }

    public List<List<FluidStack>> getUnwrapped() {
        if (unwrapped == null) {
            unwrapped = stacks.stream()
                    .map(tagOrFluid -> {
                        if (tagOrFluid == null) {
                            return null;
                        }
                        return tagOrFluid.map(
                                tagList -> tagList
                                        .stream()
                                        .flatMap(pair -> BuiltInRegistries.FLUID.getTag(pair.getFirst())
                                                .map(holderSet -> holderSet.stream()
                                                        .map(holder -> new FluidStack(holder.value(),
                                                                pair.getSecond())))
                                                .orElseGet(Stream::empty))
                                        .toList(),
                                Function.identity());
                    })
                    .collect(Collectors.toList());
        }
        return unwrapped;
    }

    @Override
    public int getTanks() {
        return stacks.size();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        List<FluidStack> stackList = getUnwrapped().get(tank);
        return stackList == null || stackList.isEmpty() ? FluidStack.EMPTY :
                stackList.get(Math.abs((int) (System.currentTimeMillis() / 1000) % stackList.size()));
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        if (tank >= 0 && tank < stacks.size()) {
            stacks.set(tank, Either.right(List.of(fluidStack)));
            unwrapped = null;
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return getFluidInTank(tank).getAmount();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public boolean supportsFill(int tank) {
        return false;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public boolean supportsDrain(int tank) {
        return false;
    }
}
