package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class NotifiableCWUTrait extends NotifiableRecipeHandlerTrait<Integer> implements ICapabilityTrait {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableCWUTrait.class);

    @Getter
    @Setter
    private long timeStamp;
    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;
    private int available;

    public NotifiableCWUTrait(MetaMachine machine, IO handlerIO, IO capabilityIO) {
        super(machine);
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
        if (machine instanceof IOpticalComputationReceiver computationReceiver) {
            int sum = left.stream().reduce(0, Integer::sum);
            var provider = computationReceiver.getComputationProvider();
            if (io == IO.IN) {
                int capacity = provider.getMaxCWUt();
                if (capacity > 0) {
                    sum = sum - capacity;
                }
            } else if (io == IO.OUT) {
                if (simulate) {
                    available = provider.requestCWUt(sum, true);
                }
                sum = sum - available;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }
        return left;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return CWURecipeCapability.CAP;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

