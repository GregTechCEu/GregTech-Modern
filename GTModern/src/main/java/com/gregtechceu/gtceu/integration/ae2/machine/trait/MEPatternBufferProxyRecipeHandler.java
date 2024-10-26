package com.gregtechceu.gtceu.integration.ae2.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MEPatternBufferProxyRecipeHandler<T> extends NotifiableRecipeHandlerTrait<T> {

    private final IO handlerIO;
    private final RecipeCapability<T> capability;

    @Setter
    private Collection<NotifiableRecipeHandlerTrait<T>> handlers = Collections.emptyList();

    public MEPatternBufferProxyRecipeHandler(MetaMachine machine, IO handlerIO, RecipeCapability<T> capability) {
        super(machine);
        this.handlerIO = handlerIO;
        this.capability = capability;
    }

    @Override
    public List<T> handleRecipeInner(IO io, GTRecipe recipe, List<T> left, @Nullable String slotName,
                                     boolean simulate) {
        for (IRecipeHandler<T> handler : handlers) {
            handler.handleRecipeInner(io, recipe, left, slotName, simulate);
            if (left.isEmpty()) return null;
        }
        return left;
    }

    @Override
    public List<Object> getContents() {
        List<Object> contents = new ObjectArrayList<>(2);
        for (NotifiableRecipeHandlerTrait<T> handler : handlers) {
            contents.addAll(handler.getContents());
        }
        return contents;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (NotifiableRecipeHandlerTrait<T> handlerTrait : handlers) {
            size += handlerTrait.getSize();
        }
        return size;
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (NotifiableRecipeHandlerTrait<T> handlerTrait : handlers) {
            amount += handlerTrait.getTotalContentAmount();
        }
        return amount;
    }

    @Override
    public boolean isDistinct() {
        for (NotifiableRecipeHandlerTrait<T> handler : handlers) {
            if (!handler.isDistinct())
                return false;
        }
        return true;
    }

    @Override
    public void setDistinct(boolean distinct) {
        handlers.forEach(handler -> handler.setDistinct(distinct));
    }

    @Override
    public void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
        handlers.forEach(handler -> handler.preWorking(holder, io, recipe));
    }

    @Override
    public void postWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
        handlers.forEach(handler -> handler.postWorking(holder, io, recipe));
    }

    @Override
    public RecipeCapability<T> getCapability() {
        return capability;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }
}
