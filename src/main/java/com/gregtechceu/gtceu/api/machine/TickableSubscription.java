package com.gregtechceu.gtceu.api.machine;

import lombok.Getter;

public class TickableSubscription {

    private final Runnable runnable;
    @Getter
    private boolean stillSubscribed;

    public TickableSubscription(Runnable runnable) {
        this.runnable = runnable;
        this.stillSubscribed = true;
    }

    public void run() {
        if (stillSubscribed) {
            runnable.run();
        }
    }

    public void unsubscribe() {
        stillSubscribed = false;
    }
}
