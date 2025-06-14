package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;

import java.util.function.BooleanSupplier;

/**
 * Handles a subscription that is only active in specific conditions.
 * <p>
 * When the subscription is not currently active, it will be removed from the event loop, in order to not unnecessarily
 * consume resources.
 */
public class ConditionalSubscriptionHandler {

    private final ITickSubscription handler;
    private final Runnable runnable;
    private final BooleanSupplier condition;

    private TickableSubscription subscription;

    public ConditionalSubscriptionHandler(ITickSubscription handler, Runnable runnable, BooleanSupplier condition) {
        this.handler = handler;
        this.runnable = runnable;
        this.condition = condition;
    }

    /**
     * Initializes the subscription and, if the supplied level is server-side, adds it to the event loop.
     *
     * @param level The level to create the subscription in.
     */
    public void initialize(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            this.initialize(serverLevel.getServer());
        }
    }

    /**
     * Initializes the subscription and adds it to the event loop.
     *
     * @param server The event loop to create the subscription in. This is usually the {@link MinecraftServer}.
     */
    protected void initialize(BlockableEventLoop<TickTask> server) {
        server.tell(new TickTask(0, this::updateSubscription));
    }

    /**
     * Updates the subscription according to whether it should currently be active.
     */
    public void updateSubscription() {
        if (condition.getAsBoolean()) {
            subscription = handler.subscribeServerTick(subscription, runnable);
        } else if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    /**
     * Unsubscribes the subscription from the event loop.
     */
    public void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }
}
