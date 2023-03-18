package com.gregtechceu.gtceu.api.data.advancement;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface IAdvancementCriterion extends CriterionTriggerInstance {

    boolean test(ServerPlayer player);

    void setId(ResourceLocation id);
}
