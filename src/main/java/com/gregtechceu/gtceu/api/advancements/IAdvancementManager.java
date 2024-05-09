package com.gregtechceu.gtceu.api.advancements;

public interface IAdvancementManager {
    <T extends IAdvancementCriterion> IAdvancementTrigger<T> registerTrigger(String id, T criterion);
}
