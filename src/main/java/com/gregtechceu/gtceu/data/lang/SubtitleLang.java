package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class SubtitleLang {

    public static void init(GTLangProvider provider) {
        generateSubtitleKeys(provider);
    }

    private static void generateSubtitleKeys(GTLangProvider provider) {
        // Subtitles
        // Machines
        provider.add("subtitles.gtceu.boiler", "Boiler heating");
        provider.add("subtitles.gtceu.computation", "Computer beeps");
        provider.add("subtitles.gtceu.assembler", "Assembler constructing");
        provider.add("subtitles.gtceu.compressor", "Compressor squeezing");
        provider.add("subtitles.gtceu.centrifuge", "Centrifuge spinning");
        provider.add("subtitles.gtceu.miner", "Miner excavating");
        provider.add("subtitles.gtceu.turbine", "Turbine whizzing");
        provider.add("subtitles.gtceu.portal_opening", "Portal opens");
        provider.add("subtitles.gtceu.replicator", "Replicator copying");
        provider.add("subtitles.gtceu.arc", "Arcs buzzing");
        provider.add("subtitles.gtceu.combustion", "Combusting");
        provider.add("subtitles.gtceu.portable_scanner", "Scanning");
        provider.add("subtitles.gtceu.macerator", "Macerator crushing");
        provider.add("subtitles.gtceu.jet_engine", "Jet roaring");
        provider.add("subtitles.gtceu.mixer", "Mixer sloshing");
        provider.add("subtitles.gtceu.fire", "Fire crackling");
        provider.add("subtitles.gtceu.forge_hammer", "Forge Hammer thumping");
        provider.add("subtitles.gtceu.bath", "Bath fizzing");
        provider.add("subtitles.gtceu.furnace", "Furnace heating");
        provider.add("subtitles.gtceu.electrolyzer", "Electrolyzer sparking");
        provider.add("subtitles.gtceu.cooling", "Freezer humming");

        // Crafting Tools
        provider.add("subtitles.gtceu.soft_hammer", "Soft tap");
        provider.add("subtitles.gtceu.wirecutter", "Wire snipped");
        provider.add("subtitles.gtceu.chemical", "Chemical bubbling");
        provider.add("subtitles.gtceu.file", "File rasping");
        provider.add("subtitles.gtceu.portal_closing", "Portal closes");
        provider.add("subtitles.gtceu.motor", "Motor humming");
        provider.add("subtitles.gtceu.drill", "Drilling");
        provider.add("subtitles.gtceu.cut", "Cutter whirring");
        provider.add("subtitles.gtceu.plunger", "Plunger popping");
        provider.add("subtitles.gtceu.mortar", "Mortar crushing");
        provider.add("subtitles.gtceu.screwdriver", "Screwing");
        provider.add("subtitles.gtceu.saw", "Sawing");
        provider.add("subtitles.gtceu.chainsaw", "Chainsaw revving");
        provider.add("subtitles.gtceu.wrench", "Wrench rattling");

        provider.add("subtitles.gtceu.spray_can", "Spraying");

        provider.add("subtitles.gtceu.sus", "Sus...");
        provider.add("subtitles.gtceu.science", "s c i e n c e");
        provider.add("subtitles.gtceu.metal_pipe", "Destruction_Metal_Pole_L_Wave_2_0_0.wav");
    }
}
