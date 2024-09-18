package com.gregtechceu.gtceu.data.advancements;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GTMiscAdvancements {
    public static Advancement rootSteam;
    public static void init(@NotNull RegistrateAdvancementProvider provider) {
        provider.accept(rootSteam = Advancement.Builder.advancement()
                .display(
                        GTMachines.STEAM_SOLID_BOILER.first().getBlock(),
                        Component.translatable("gtceu.advancement.root_steam.name"),
                        Component.translatable("gtceu.advancement.root_steam.desc"),
                        GTCEu.id("textures/gui/advancements/background.png"),
                        FrameType.TASK,
                        true,
                        true,
                        false
                )
                  .addCriterion("join", PlayerTrigger.TriggerInstance.sleptInBed())
                .save(provider, GTCEu.MOD_ID + ":root_steam"));
    }
}
