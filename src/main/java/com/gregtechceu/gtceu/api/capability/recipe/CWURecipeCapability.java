package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;

import java.util.List;

public class CWURecipeCapability extends RecipeCapability<Integer> {

    public final static CWURecipeCapability CAP = new CWURecipeCapability();

    protected CWURecipeCapability() {
        super(GTCEu.id("cwu"), 0xFFEEEE00, false, 3, SerializerInteger.INSTANCE);
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
    public boolean skipEmptyContentCheck() {
        return true;
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {
        if (perTick) {
            int cwu = contents.stream().map(Content::getContent).mapToInt(CWURecipeCapability.CAP::of).sum();
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("gtceu.recipe.computation_per_tick", FormattingUtil.formatNumbers(cwu))));
        }
        if (recipe.data.getBoolean("duration_is_total_cwu")) {
            group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10),
                    LocalizationUtils.format("gtceu.recipe.total_computation",
                            FormattingUtil.formatNumbers(recipe.duration))));
        }
    }
  
    public List<NotifiableComputationContainer> getCapabilityHandlers(MetaMachine machine) {
        return machine.getTraits(NotifiableComputationContainer.TYPE);
    }

    @Override
    public List<NotifiableComputationContainer> getCapabilityHandlers(MetaMachine machine, IO io) {
        return getCapabilityHandlers(machine).stream()
                .filter(v -> v.getHandlerIO() == io).toList();
    }
}
