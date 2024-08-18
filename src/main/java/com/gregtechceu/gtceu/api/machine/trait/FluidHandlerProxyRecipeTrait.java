package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class FluidHandlerProxyRecipeTrait extends NotifiableRecipeHandlerTrait<FluidIngredient>
                                          implements ICapabilityTrait {

    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Getter
    @Setter
    private long timeStamp;
    private boolean enabled;

    @Getter
    private final Collection<NotifiableRecipeHandlerTrait<FluidIngredient>> handlers;

    public FluidHandlerProxyRecipeTrait(MetaMachine machine,
                                        Collection<NotifiableRecipeHandlerTrait<FluidIngredient>> handlers,
                                        IO handlerIO,
                                        IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
        this.handlers = handlers;
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                   @Nullable String slotName, boolean simulate) {
        if (!enabled) return left;
        for (IRecipeHandler<FluidIngredient> handler : handlers) {
            handler.handleRecipeInner(io, recipe, left, slotName, simulate);
            if (left.isEmpty()) return null;
        }
        return left;
    }

    @Override
    public List<Object> getContents() {
        List<Object> contents = new ObjectArrayList<>(2);
        for (NotifiableRecipeHandlerTrait<FluidIngredient> handler : handlers) {
            contents.addAll(handler.getContents());
        }
        return contents;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (NotifiableRecipeHandlerTrait<FluidIngredient> handlerTrait : handlers) {
            size += handlerTrait.getSize();
        }
        return size;
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (NotifiableRecipeHandlerTrait<FluidIngredient> handlerTrait : handlers) {
            amount += handlerTrait.getTotalContentAmount();
        }
        return amount;
    }

    @Override
    public RecipeCapability<FluidIngredient> getCapability() {
        return FluidRecipeCapability.CAP;
    }

    @Override
    public boolean isDistinct() {
        for (NotifiableRecipeHandlerTrait<FluidIngredient> handler : handlers) {
            if (!handler.isDistinct)
                return false;
        }
        return true;
    }

    @Override
    public void setDistinct(boolean distinct) {
        handlers.forEach(handler -> handler.setDistinct(distinct));
        recomputeEnabledState();
    }

    @Override
    public boolean isProxy() {
        return true;
    }

    public void recomputeEnabledState() {
        this.enabled = isDistinct();
    }
}
