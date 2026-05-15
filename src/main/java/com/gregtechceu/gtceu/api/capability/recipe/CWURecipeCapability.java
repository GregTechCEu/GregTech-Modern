package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;

import java.util.List;

public class CWURecipeCapability extends RecipeCapability<Integer> {

    public final static CWURecipeCapability CAP = new CWURecipeCapability();

    protected CWURecipeCapability() {
        super("cwu", 0xFFEEEE00, false, 3, SerializerInteger.INSTANCE);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyWithModifier(Integer content, ContentModifier modifier) {
        return modifier.apply(content);
    }

    @Override
    public List<NotifiableComputationContainer> getCapabilityHandlers(MetaMachine machine) {
        return machine.getTraits(NotifiableComputationContainer.TYPE);
    }

    @Override
    public List<NotifiableComputationContainer> getCapabilityHandlers(MetaMachine machine, IO io) {
        return getCapabilityHandlers(machine).stream()
                .filter(v -> v.getHandlerIO() == io).toList();
    }
}
