package com.gregtechceu.gtceu.utils.function;

@FunctionalInterface
public interface Task {

    /**
     * Run the actions of this Task. Will be infinitely run each world tick until false is returned.
     *
     * @return {@code true} if the task should be run again, otherwise {@code false}
     */
    boolean run();
}
