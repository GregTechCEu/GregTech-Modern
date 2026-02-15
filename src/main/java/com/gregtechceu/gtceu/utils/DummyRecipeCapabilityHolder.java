package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;

import lombok.Getter;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DummyRecipeCapabilityHolder implements IRecipeCapabilityHolder {

    @Getter
    protected final Map<IO, List<RecipeHandlerList>> capabilitiesProxy;
    @Getter
    protected final Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> capabilitiesFlat;

    public DummyRecipeCapabilityHolder(RecipeHandlerList... handlers) {
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        for (RecipeHandlerList handler : handlers) {
            addHandlerList(handler);
        }
    }
}
