package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.LaserRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NotifiableLaserContainer extends NotifiableRecipeHandlerTrait<Long> implements ILaserContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableEnergyContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);
    @Getter
    protected IO handlerIO;
    @Getter
    @Setter
    private long timeStamp;
    @Getter
    @Persisted @DescSynced
    protected long energyStored;

    public NotifiableLaserContainer(MetaMachine machine) {
        super(machine);
    }

    @Override
    public long changeEnergy(long amount, @NotNull Collection<ILaserContainer> seen) {
        seen.add(this);
        return 0;
    }

    @Override
    public long getEnergyStored(@NotNull Collection<ILaserContainer> seen) {
        seen.add(this);
        return energyStored;
    }

    @Override
    public long getEnergyCapacity(@NotNull Collection<ILaserContainer> seen) {
        seen.add(this);
        return 0;
    }

    @Override
    public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> left, @Nullable String slotName, boolean simulate) {
        ILaserContainer capability = this;
        long sum = left.stream().reduce(0L, Long::sum);
        if (io == IO.IN) {
            var canOutput = capability.getEnergyStored();
            if (!simulate) {
                capability.changeEnergy(-Math.min(canOutput, sum));
            }
            sum = sum - canOutput;
        } else if (io == IO.OUT) {
            long canInput = capability.getEnergyCapacity() - capability.getEnergyStored();
            if (!simulate) {
                capability.changeEnergy(Math.min(canInput, sum));
            }
            sum = sum - canInput;
        }
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Override
    public RecipeCapability<Long> getCapability() {
        return LaserRecipeCapability.CAP;
    }
}
