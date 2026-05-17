package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class NotifiableRecipeHandlerTrait<T> extends MachineTrait implements IRecipeHandlerTrait<T> {

    protected List<Runnable> listeners = new ArrayList<>();

    @Persisted
    @DescSynced
    @Getter
    @Setter
    protected boolean isDistinct;

    public NotifiableRecipeHandlerTrait(MetaMachine machine) {
        super(machine);
    }

    @Override
    public ISubscription addChangedListener(Runnable listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
