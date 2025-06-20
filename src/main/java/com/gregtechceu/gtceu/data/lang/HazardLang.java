package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangUtil.*;

public class HazardLang {

    public static void init(RegistrateLangProvider provider) {
        generateHazardKeys(provider);
    }

    private static void generateHazardKeys(RegistrateLangProvider provider) {
        provider.add("gtceu.medical_condition.description", "§l§cHAZARDOUS §7Hold SHIFT to show details");
        provider.add("gtceu.medical_condition.description_shift", "§l§cHAZARDOUS:");
        provider.add("gtceu.medical_condition.chemical_burns", "§5Chemical burns");
        provider.add("gtceu.medical_condition.poison", "§2Poisonous");
        provider.add("gtceu.medical_condition.weak_poison", "§aWeakly poisonous");
        provider.add("gtceu.medical_condition.irritant", "§6Irritant");
        provider.add("gtceu.medical_condition.nausea", "§3Nauseating");
        provider.add("gtceu.medical_condition.carcinogen", "§eCarcinogenic");
        provider.add("gtceu.medical_condition.asbestosis", "§dAsbestosis");
        provider.add("gtceu.medical_condition.arsenicosis", "§bArsenicosis");
        provider.add("gtceu.medical_condition.silicosis", "§1Silicosis");
        provider.add("gtceu.medical_condition.berylliosis", "§5Berylliosis");
        provider.add("gtceu.medical_condition.methanol_poisoning", "§6Methanol Poisoning");
        provider.add("gtceu.medical_condition.carbon_monoxide_poisoning", "§7Carbon Monoxide Poisoning");
        provider.add("gtceu.medical_condition.none", "§2Not Dangerous");
        provider.add("gtceu.hazard_trigger.description", "Caused by:");
        provider.add("gtceu.hazard_trigger.protection.description", "Protects from:");
        provider.add("gtceu.hazard_trigger.inhalation", "Inhalation");
        provider.add("gtceu.hazard_trigger.any", "Any contact");

        provider.add("gtceu.hazard_trigger.skin_contact", "Skin contact");
        provider.add("gtceu.hazard_trigger.none", "Nothing");
        provider.add("gtceu.medical_condition.antidote.description", "§aAntidote §7Hold SHIFT to show details");
        provider.add("gtceu.medical_condition.antidote.description_shift", "§aCures these conditions:");
        provider.add("gtceu.medical_condition.antidote.description.effect_removed",
                "Removes %s%% of current conditions' effects");
        provider.add("gtceu.medical_condition.antidote.description.effect_removed.all",
                "Removes all of current conditions' effects");
    }
}
