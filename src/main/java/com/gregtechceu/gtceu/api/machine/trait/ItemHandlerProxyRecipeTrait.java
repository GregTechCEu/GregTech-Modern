package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.neoforged.neoforge.common.crafting.SizedIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ItemHandlerProxyRecipeTrait extends NotifiableRecipeHandlerTrait<SizedIngredient>
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
    private final Collection<NotifiableRecipeHandlerTrait<SizedIngredient>> handlers;

    @Setter
    private Supplier<NotifiableRecipeHandlerTrait<SizedIngredient>> handlerSupplier;

    public ItemHandlerProxyRecipeTrait(MetaMachine machine,
                                       Collection<NotifiableRecipeHandlerTrait<SizedIngredient>> handlers, IO handlerIO,
                                       IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
        this.handlers = handlers;
    }

    @Override
    public List<SizedIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedIngredient> left,
                                                   @Nullable String slotName, boolean simulate) {
        if (!enabled) return left;
        for (IRecipeHandler<SizedIngredient> handler : handlers) {
            handler.handleRecipeInner(io, recipe, left, slotName, simulate);
            if (left.isEmpty()) return null;
        }
        return left;
    }

    @Override
    public List<Object> getContents() {
        List<Object> contents = new ObjectArrayList<>(2);
        for (NotifiableRecipeHandlerTrait<SizedIngredient> handler : handlers) {
            contents.addAll(handler.getContents());
        }
        return contents;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (NotifiableRecipeHandlerTrait<SizedIngredient> handlerTrait : handlers) {
            size += handlerTrait.getSize();
        }
        return size;
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (NotifiableRecipeHandlerTrait<SizedIngredient> handlerTrait : handlers) {
            amount += handlerTrait.getTotalContentAmount();
        }
        return amount;
    }

    @Override
    public RecipeCapability<SizedIngredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }

    @Override
    public boolean isDistinct() {
        for (NotifiableRecipeHandlerTrait<SizedIngredient> handler : handlers) {
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
