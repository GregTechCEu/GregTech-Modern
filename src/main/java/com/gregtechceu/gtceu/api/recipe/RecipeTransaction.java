package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;

import java.util.ArrayDeque;

public final class RecipeTransaction implements AutoCloseable {

    private static final ThreadLocal<RecipeTransaction> CURRENT = new ThreadLocal<>();

    private final RecipeTransaction parent;
    private ArrayDeque<Runnable> undo;
    private boolean finished;

    private RecipeTransaction() {
        parent = CURRENT.get();
        CURRENT.set(this);
    }

    public static RecipeTransaction begin() {
        return new RecipeTransaction();
    }

    public static boolean isActive() {
        return CURRENT.get() != null;
    }

    public static void record(Runnable undoAction) {
        var transaction = CURRENT.get();
        if (transaction == null || transaction.finished) return;
        transaction.push(undoAction);
    }

    public void commit() {
        if (finished) return;
        finished = true;
        if (parent != null && undo != null) {
            for (var it = undo.descendingIterator(); it.hasNext();) {
                parent.push(it.next());
            }
        }
        undo = null;
    }

    public void rollback() {
        finished = true;
        if (undo == null) return;
        Runnable action;
        while ((action = undo.poll()) != null) {
            try {
                action.run();
            } catch (RuntimeException e) {
                GTCEu.LOGGER.error("Recipe rollback failed", e);
            }
        }
    }

    private void push(Runnable action) {
        if (undo == null) undo = new ArrayDeque<>(4);
        undo.push(action);
    }

    @Override
    public void close() {
        if (!finished) rollback();
        if (parent == null) CURRENT.remove();
        else CURRENT.set(parent);
    }
}
