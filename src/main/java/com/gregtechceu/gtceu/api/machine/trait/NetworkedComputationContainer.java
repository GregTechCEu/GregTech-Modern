package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.computation.ComputationConsumer;
import com.gregtechceu.gtceu.api.computation.ComputationProducer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.computation.ComputationNetworkManager;

import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NetworkedComputationContainer extends NotifiableRecipeHandlerTrait<Integer>
                                           implements ComputationProducer, ComputationConsumer {

    @Getter
    private final IO handlerIO;
    @Getter
    private int receivedCWUt;

    public NetworkedComputationContainer(MetaMachine machine, IO handlerIO) {
        super(machine);
        this.handlerIO = handlerIO;
    }

    @Override
    public boolean handleRecipe(IO io, GTRecipe recipe, List<Integer> left, boolean simulate) {
        int sum = left.stream().mapToInt(Integer::intValue).sum();
        if (io == IO.OUT) {
            left.clear();
            return true;
        }
        if (simulate && machine.getLevel() instanceof ServerLevel serverLevel) {
            ComputationConsumer target = machine instanceof ComputationConsumer consumer ? consumer : this;
            if (ComputationNetworkManager.get(serverLevel).reserveDemand(target, sum)) {
                left.clear();
                return true;
            }
            return false;
        }

        if (receivedCWUt < sum) {
            return false;
        }

        if (!simulate && recipe.data.getBoolean("duration_is_total_cwu") && machine instanceof IRecipeLogicMachine rlm) {
            rlm.getRecipeLogic().progress -= 1;
            rlm.getRecipeLogic().progress += receivedCWUt;
        }
        left.clear();
        return true;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(handlerIO.support(IO.OUT) ? getOfferedCWUt() : receivedCWUt);
    }

    @Override
    public double getTotalContentAmount() {
        return handlerIO.support(IO.OUT) ? getOfferedCWUt() : receivedCWUt;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return CWURecipeCapability.CAP;
    }

    @Override
    public int getOfferedCWUt() {
        if (!handlerIO.support(IO.OUT)) return 0;
        GTRecipe recipe = getLastRecipe();
        if (recipe == null) return 0;
        return recipe.getTickOutputContents(CWURecipeCapability.CAP).stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public int getMinimumCWUt() {
        if (!handlerIO.support(IO.IN)) return 0;
        GTRecipe recipe = getLastRecipe();
        if (recipe == null) return 0;
        return recipe.getTickInputContents(CWURecipeCapability.CAP).stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public int getRequestedCWUt() {
        GTRecipe recipe = getLastRecipe();
        if (recipe != null && recipe.data.getBoolean("duration_is_total_cwu")) {
            return Integer.MAX_VALUE;
        }
        if (recipe == null) return 0;
        return getMinimumCWUt();
    }

    @Override
    public void applyReceivedCWUt(int receivedCWUt) {
        this.receivedCWUt = Math.max(0, receivedCWUt);
    }

    @Override
    public void onComputationChanged() {
        notifyListeners();
    }

    private GTRecipe getLastRecipe() {
        return machine instanceof IRecipeLogicMachine rlm ? rlm.getRecipeLogic().getLastRecipe() : null;
    }
}
