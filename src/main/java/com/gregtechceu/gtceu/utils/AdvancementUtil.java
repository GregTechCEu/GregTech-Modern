package com.gregtechceu.gtceu.utils;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

public class AdvancementUtil {
    public static Advancement getAdvancementById(ResourceLocation advancementID) {
        return ServerLifecycleHooks.getCurrentServer().getAdvancements().getAdvancement(advancementID);
    }

    public static boolean awardAdvancement(ServerPlayer player, ResourceLocation advancementID, String advancementCriteria) {
        return player.getAdvancements().award(getAdvancementById(advancementID), advancementCriteria);
    }
}
