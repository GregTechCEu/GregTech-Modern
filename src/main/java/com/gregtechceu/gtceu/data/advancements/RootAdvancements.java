package com.gregtechceu.gtceu.data.advancements;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.network.chat.Component;

public class RootAdvancements {
    public static Advancement ROOT_STEAM;
    public static Advancement ROOT_LV;

    public static void init(RegistrateAdvancementProvider provider) {
        provider.accept(ROOT_STEAM = Advancement.Builder.advancement()
                .display(
                        GTBlocks.BRONZE_HULL.asItem(),
                        Component.translatable("gtceu.advancement.root_steam.title"),
                        Component.translatable("gtceu.advancement.root_steam.desc"),
                        GTCEu.id("textures/gui/advancements/background.png"),
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .addCriterion("gt_placeholder_criteria", new ImpossibleTrigger.TriggerInstance())
                .build(GTCEu.id("steam/root"))
        );

        provider.accept(ROOT_LV = Advancement.Builder.advancement()
                .display(
                        GTBlocks.MACHINE_CASING_LV.asItem(),
                        Component.translatable("gtceu.advancement.root_lv.title"),
                        Component.translatable("gtceu.advancement.root_lv.desc"),
                        GTCEu.id("textures/gui/advancements/background.png"),
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .addCriterion("gt_placeholder_criteria", new ImpossibleTrigger.TriggerInstance())
                .build(GTCEu.id("low_voltage/root"))
        );
    }
}
