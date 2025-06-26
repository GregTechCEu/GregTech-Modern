package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class HazardLang {

    public static void init(GTLangProvider provider) {
        generateHazardKeys(provider);
    }

    private static void generateHazardKeys(GTLangProvider provider) {
        // Medical Conditions
        provider.add("gtceu.medical_condition.description", "§l§cHAZARDOUS §7Hold Shift to show details");
        provider.add("gtceu.medical_condition.description_shift", "§l§cHAZARDOUS:");
        provider.add("gtceu.medical_condition/chemical_burns", "§5Chemical burns");
        provider.add("gtceu.medical_condition/poison", "§2Poisonous");
        provider.add("gtceu.medical_condition/weak_poison", "§aWeakly poisonous");
        provider.add("gtceu.medical_condition/irritant", "§6Irritant");
        provider.add("gtceu.medical_condition/nausea", "§3Nauseating");
        provider.add("gtceu.medical_condition/carcinogen", "§eCarcinogenic");
        provider.add("gtceu.medical_condition/asbestosis", "§dAsbestosis");
        provider.add("gtceu.medical_condition/arsenicosis", "§bArsenicosis");
        provider.add("gtceu.medical_condition/silicosis", "§1Silicosis");
        provider.add("gtceu.medical_condition/berylliosis", "§5Berylliosis");
        provider.add("gtceu.medical_condition/methanol_poisoning", "§6Methanol Poisoning");
        provider.add("gtceu.medical_condition/carbon_monoxide_poisoning", "§7Carbon Monoxide Poisoning");
        provider.add("gtceu.medical_condition/none", "§2Not Dangerous");

        // Hazard Triggers
        provider.add("gtceu.hazard_trigger.description", "Caused by:");
        provider.add("gtceu.hazard_trigger.protection.description", "Protects from:");
        provider.add("gtceu.hazard_trigger.inhalation", "Inhalation");
        provider.add("gtceu.hazard_trigger.any", "Any contact");
        provider.add("gtceu.hazard_trigger.skin_contact", "Skin contact");
        provider.add("gtceu.hazard_trigger.none", "Nothing");

        // Antidote
        provider.add("gtceu.medical_condition.antidote.description", "§aAntidote §7Hold Shift to show details");
        provider.add("gtceu.medical_condition.antidote.description_shift", "§aCures these conditions:");
        provider.add("gtceu.medical_condition.antidote.description.effect_removed",
                "Removes %s%% of current conditions' effects");
        provider.add("gtceu.medical_condition.antidote.description.effect_removed.all",
                "Removes all of current conditions' effects");

        // Potion
        provider.add("gtceu.tooltip.potion.header", "§6Contains effects:");
        provider.add("gtceu.tooltip.potion.each", "%s %s §7for§r %s §7ticks with a§r %s%% §7chance of happening§r");

        // Poison
        provider.add("effect.gtceu.weak_poison", "Weak Poison");

        // Medical Deaths
        provider.add("death.attack.gtceu.medical_condition.asbestosis", "%s got mesothelioma");
        provider.add("death.attack.gtceu.medical_condition.chemical_burns", "%s had a chemical accident");
        provider.add("death.attack.gtceu.medical_condition.poison",
                "%s forgot that poisonous materials are, in fact, poisonous");
        provider.add("death.attack.gtceu.medical_condition.silicosis",
                "%s didn't die of tuberculosis. it was silicosis.");
        provider.add("death.attack.gtceu.medical_condition.arsenicosis", "%s got arsenic poisoning");
        provider.add("death.attack.gtceu.medical_condition.berylliosis", "%s mined emeralds a bit too greedily");
        provider.add("death.attack.gtceu.medical_condition.carcinogen", "%s got leukemia");
        provider.add("death.attack.gtceu.medical_condition.irritant", "%s got a §n§lREALLY§r bad rash");
        provider.add("death.attack.gtceu.medical_condition.methanol_poisoning",
                "%s tried to drink moonshine during the prohibition");
        provider.add("death.attack.gtceu.medical_condition.nausea", "%s died of nausea");
        provider.add("death.attack.gtceu.medical_condition.none", "%s died of... nothing?");
        provider.add("death.attack.gtceu.medical_condition.weak_poison", "%s ate lead (or mercury!)");
        provider.add("death.attack.gtceu.medical_condition.carbon_monoxide_poisoning", "%s left the stove on");
    }
}
