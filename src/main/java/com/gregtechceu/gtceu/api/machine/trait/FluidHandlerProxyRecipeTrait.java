package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class FluidHandlerProxyRecipeTrait extends NotifiableRecipeHandlerTrait<SizedFluidIngredient>
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
    private final Collection<NotifiableRecipeHandlerTrait<SizedFluidIngredient>> handlers;

    public FluidHandlerProxyRecipeTrait(MetaMachine machine,
                                        Collection<NotifiableRecipeHandlerTrait<SizedFluidIngredient>> handlers,
                                        IO handlerIO,
                                        IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
        this.handlers = handlers;
    }

    @Override
    public List<SizedFluidIngredient> handleRecipeInner(IO io, GTRecipe recipe,
                                                        List<SizedFluidIngredient> left,
                                                        @Nullable String slotName, boolean simulate) {
        if (!enabled) return left;
        for (IRecipeHandler<SizedFluidIngredient> handler : handlers) {
            handler.handleRecipeInner(io, recipe, left, slotName, simulate);
            if (left.isEmpty()) return null;
        }
        return left;
    }

    @Override
    public List<Object> getContents() {
        List<Object> contents = new ObjectArrayList<>(2);
        for (NotifiableRecipeHandlerTrait<SizedFluidIngredient> handler : handlers) {
            contents.addAll(handler.getContents());
        }
        return contents;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (NotifiableRecipeHandlerTrait<SizedFluidIngredient> handlerTrait : handlers) {
            size += handlerTrait.getSize();
        }
        return size;
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (NotifiableRecipeHandlerTrait<SizedFluidIngredient> handlerTrait : handlers) {
            amount += handlerTrait.getTotalContentAmount();
        }
        return amount;
    }

    @Override
    public RecipeCapability<SizedFluidIngredient> getCapability() {
        return FluidRecipeCapability.CAP;
    }

    @Override
    public boolean isDistinct() {
        for (NotifiableRecipeHandlerTrait<SizedFluidIngredient> handler : handlers) {
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
