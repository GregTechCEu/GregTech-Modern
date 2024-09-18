package com.gregtechceu.gtceu.data.advancements;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import net.minecraft.resources.ResourceLocation;

public class LVAdvancements {

    public static Advancement FIRST_COVER_PLACE;

    public static void init(RegistrateAdvancementProvider provider) {
        provider.accept(FIRST_COVER_PLACE = Advancement.Builder.advancement()
                .display(
                        GTItems.COVER_ITEM_DETECTOR.asItem(),
                        Component.translatable("gtceu.advancement.low_voltage.88_first_cover_place.title"),
                        Component.translatable("gtceu.advancement.low_voltage.88_first_cover_place.desc"),
                        GTCEu.id("textures/gui/advancements/background.png"),
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .build(GTCEu.id("low_voltage/first_cover_place"))
        );
    }
}
