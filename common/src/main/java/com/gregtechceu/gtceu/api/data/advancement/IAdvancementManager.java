package com.gregtechceu.gtceu.api.data.advancement;

public interface IAdvancementManager {
    <T extends IAdvancementCriterion> IAdvancementTrigger<T> registerTrigger(String id, T criterion);
}
