package com.gregtechceu.gtceu.api.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public interface IAdvancementTrigger<T extends IAdvancementCriterion> extends CriterionTrigger<T> {
    void trigger(ServerPlayer player);
}
