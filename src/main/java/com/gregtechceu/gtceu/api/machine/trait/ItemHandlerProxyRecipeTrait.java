package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ItemHandlerProxyRecipeTrait extends NotifiableRecipeHandlerTrait<Ingredient> implements ICapabilityTrait {
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    @Getter @Setter
    private long timeStamp;
    private boolean enabled;

    @Getter
    private final Collection<NotifiableRecipeHandlerTrait<Ingredient>> handlers;

    public ItemHandlerProxyRecipeTrait(MetaMachine machine, Collection<NotifiableRecipeHandlerTrait<Ingredient>> handlers, IO handlerIO, IO capabilityIO) {
        super(machine);
        this.timeStamp = Long.MIN_VALUE;
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
        this.handlers = handlers;
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName, boolean simulate) {
        if (!enabled) return left;
        for (IRecipeHandler<Ingredient> handler : handlers) {
            handler.handleRecipeInner(io, recipe, left, slotName, simulate);
            if (left.isEmpty()) return null;
        }
        return left;
    }

    @Override
    public RecipeCapability<Ingredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }

    @Override
    public boolean isDistinct() {
        return handlers.stream().allMatch(NotifiableRecipeHandlerTrait::isDistinct);
    }

    @Override
    public void setDistinct(boolean distinct) {
        handlers.stream().forEach(handler -> handler.setDistinct(distinct));
        recomputeEnabledState();
    }

    public void recomputeEnabledState() {
        this.enabled = isDistinct();
    }
}
