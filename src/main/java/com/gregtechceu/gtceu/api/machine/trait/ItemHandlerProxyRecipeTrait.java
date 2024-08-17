package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.world.item.crafting.Ingredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ItemHandlerProxyRecipeTrait extends NotifiableRecipeHandlerTrait<Ingredient> implements ICapabilityTrait {

    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Getter
    @Setter
    private long timeStamp;
    private boolean enabled;

    @Getter
    private final Collection<NotifiableRecipeHandlerTrait<Ingredient>> handlers;

    @Setter
    private Supplier<NotifiableRecipeHandlerTrait<Ingredient>> handlerSupplier;

    public ItemHandlerProxyRecipeTrait(MetaMachine machine,
                                       Collection<NotifiableRecipeHandlerTrait<Ingredient>> handlers, IO handlerIO,
                                       IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
        this.handlers = handlers;
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName,
                                              boolean simulate) {
        if (!enabled) return left;
        for (IRecipeHandler<Ingredient> handler : handlers) {
            handler.handleRecipeInner(io, recipe, left, slotName, simulate);
            if (left.isEmpty()) return null;
        }

        if (handlerSupplier != null) {
            var handler = handlerSupplier.get();
            if (handler != null) {
                return handler.handleRecipeInner(io, recipe, left, slotName, simulate);
            }
        }
        return left;
    }

    @Override
    public List<Object> getContents() {
        List<Object> contents = new ObjectArrayList<>(2);
        for (NotifiableRecipeHandlerTrait<Ingredient> handler : handlers) {
            contents.addAll(handler.getContents());
        }

        if (handlerSupplier != null) {
            var handler = handlerSupplier.get();
            if (handler != null) {
                contents.addAll(handler.getContents());
            }
        }
        return contents;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (NotifiableRecipeHandlerTrait<Ingredient> handlerTrait : handlers) {
            size += handlerTrait.getSize();
        }

        if (handlerSupplier != null) {
            var handler = handlerSupplier.get();
            if (handler != null) {
                size += handler.getSize();
            }
        }
        return size;
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (NotifiableRecipeHandlerTrait<Ingredient> handlerTrait : handlers) {
            amount += handlerTrait.getTotalContentAmount();
        }
        if (handlerSupplier != null) {
            var handler = handlerSupplier.get();
            if (handler != null) {
                amount += handler.getTotalContentAmount();
            }
        }
        return amount;
    }

    @Override
    public RecipeCapability<Ingredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }

    @Override
    public boolean isDistinct() {
        for (NotifiableRecipeHandlerTrait<Ingredient> handler : handlers) {
            if (handler.isDistinct)
                return true;
        }

        if (handlerSupplier != null) {
            var handler = handlerSupplier.get();
            if (handler != null) {
                return handler.isDistinct();
            }
        }

        return false;
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
