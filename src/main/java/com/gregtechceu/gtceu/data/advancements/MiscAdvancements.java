package com.gregtechceu.gtceu.data.advancements;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import net.minecraft.resources.ResourceLocation;

public class MiscAdvancements {

    public static Advancement testAdvancement;

    public static void init(RegistrateAdvancementProvider provider) {
        provider.accept(testAdvancement = Advancement.Builder.advancement()
                .display(
                        GTMachines.STEAM_SOLID_BOILER.first().getBlock(),
                        Component.translatable("gtceu.advancement.root_steam.title"),
                        Component.translatable("gtceu.advancement.root_steam.desc"),
                        GTCEu.id("textures/gui/advancements/background.png"),
                        FrameType.TASK,
                        true,
                        true,
                        false)
                .build(GTCEu.id("misc/test"))
        );
    }
}
