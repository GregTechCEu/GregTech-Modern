package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.ISubscription;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class NotifiableRecipeHandlerTrait<T> extends MachineTrait implements IRecipeHandlerTrait<T> {

    protected List<Runnable> listeners = new ArrayList<>();

    @SaveField
    @SyncToClient
    @Getter
    protected boolean isDistinct;

    public NotifiableRecipeHandlerTrait() {}

    public void setDistinct(boolean distinct) {
        isDistinct = distinct;
        syncDataHolder.markClientSyncFieldDirty("isDistinct");
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
