package com.lowdragmc.gtceu.api.machine.trait;

import com.lowdragmc.gtceu.api.machine.MetaMachine;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote NotifiableTrait
 */
public abstract class NotifiableRecipeHandlerTrait<T> extends MachineTrait implements IRecipeHandlerTrait<T> {
    protected List<Runnable> listeners = new ArrayList<>();

    public NotifiableRecipeHandlerTrait(MetaMachine machine) {
        super(machine);
    }

    @Override
    public ISubscription addChangedListener(Runnable listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    protected void notifyListeners() {
        listeners.forEach(Runnable::run);
    }

}
