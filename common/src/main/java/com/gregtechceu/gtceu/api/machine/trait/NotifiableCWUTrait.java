package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.StressRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.kinetic.IKineticMachine;
import com.gregtechceu.gtceu.common.machine.trait.NotifiableStressTrait;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Mth;
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
    private float available, lastSpeed;

    public NotifiableCWUTrait(MetaMachine machine, IO handlerIO, IO capabilityIO) {
        super(machine);
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
        this.lastSpeed = 0;
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (machine instanceof IKineticMachine kineticMachine) {
            machine.subscribeServerTick(() -> {
                var speed = kineticMachine.getKineticHolder().getSpeed();
                if (speed != lastSpeed) {
                    lastSpeed = speed;
                    notifyListeners();
                }
            });
        }
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
        if (machine instanceof IOpticalComputationReceiver computationReceiver) {
            int sum = left.stream().reduce(0, Integer::sum);
            var kineticDefinition = computationReceiver.getComputationProvider();
            if (io == IO.IN && !kineticDefinition.isSource()) {
                int capacity = Mth.abs(computationReceiver.getKineticHolder().getSpeed()) * kineticDefinition.torque;
                if (capacity > 0) {
                    sum = sum - capacity;
                }
            } else if (io == IO.OUT && kineticDefinition.isSource()) {
                if (simulate) {
                    available = computationReceiver.getKineticHolder().scheduleWorking(sum, true);
                }
                sum = sum - available;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }
        return left;
    }

    @Override
    public void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
        if (machine instanceof IKineticMachine kineticMachine) {
            var kineticDefinition = kineticMachine.getKineticDefinition();
            if (available > 0 && kineticDefinition.isSource() && io == IO.OUT) {
                kineticMachine.getKineticHolder().scheduleWorking(available, false);
            }
        }
    }

    @Override
    public void postWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
        if (machine instanceof IKineticMachine kineticMachine) {
            var kineticDefinition = kineticMachine.getKineticDefinition();
            if (kineticDefinition.isSource() && io == IO.OUT) {
                kineticMachine.getKineticHolder().stopWorking();
            }
        }
    }

    @Override
    public RecipeCapability<Float> getCapability() {
        return StressRecipeCapability.CAP;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

