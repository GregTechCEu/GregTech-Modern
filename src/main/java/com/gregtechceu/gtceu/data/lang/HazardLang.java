package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class HazardLang {

    public static void init(GTLangProvider provider) {
        generateHazardKeys(provider);
    }

    private static void generateHazardKeys(GTLangProvider provider) {
        // Medical Conditions
        provider.add("tooltip.gtceu.medical_condition.description", "§l§cHAZARDOUS §7Hold Shift to show details");
        provider.add("tooltip.gtceu.medical_condition.description_shift", "§l§cHAZARDOUS:");
        provider.add("medical_condition.gtceu.chemical_burns", "§5Chemical burns");
        provider.add("medical_condition.gtceu.poison", "§2Poisonous");
        provider.add("medical_condition.gtceu.poison.affected", "§2Poisoning");
        provider.add("medical_condition.gtceu.weak_poison", "§aWeakly poisonous");
        provider.add("medical_condition.gtceu.weak_poison.affected", "§aMinor poisoning");
        provider.add("medical_condition.gtceu.irritant", "§6Irritant");
        provider.add("medical_condition.gtceu.irritant.affected", "§6Irritation");
        provider.add("medical_condition.gtceu.nausea", "§3Nauseating");
        provider.add("medical_condition.gtceu.nausea.affected", "§3Nausea");
        provider.add("medical_condition.gtceu.carcinogen", "§eCarcinogenic");
        provider.add("medical_condition.gtceu.carcinogen.affected", "§eCancer");
        provider.add("medical_condition.gtceu.asbestosis", "§dAsbestosis");
        provider.add("medical_condition.gtceu.arsenicosis", "§bArsenicosis");
        provider.add("medical_condition.gtceu.methanol_poisoning", "§6Methanol Poisoning");
        provider.add("medical_condition.gtceu.carbon_monoxide_poisoning", "§7Carbon Monoxide Poisoning");
        provider.add("medical_condition.gtceu.none", "§2Not Dangerous");
        provider.add("medical_condition.gtceu.none.affected", "§2Nothing?");

        // Symptoms
        provider.add("symptom.gtceu.death", "Death");
        provider.add("symptom.gtceu.random_damage", "Occasional damage");
        provider.add("symptom.gtceu.health_debuff", "Lowered maximum health");
        provider.add("symptom.gtceu.air_supply_debuff", "Lowered lung capacity");
        provider.add("symptom.gtceu.mining_fatigue", "Fatigue");
        provider.add("symptom.gtceu.weakness", "Weakness");
        provider.add("symptom.gtceu.slowness", "Slowness");
        provider.add("symptom.gtceu.blindness", "Blindness");
        provider.add("symptom.gtceu.darkness", "Darkness");
        provider.add("symptom.gtceu.nausea", "Nausea");
        provider.add("symptom.gtceu.wither", "Necrosis");
        provider.add("symptom.gtceu.weak_poisoning", "Weak poisoning");
        provider.add("symptom.gtceu.poisoning", "Poisoning");
        provider.add("symptom.gtceu.hunger", "Increased appetite");

        // Hazard Triggers
        provider.add("tooltip.gtceu.hazard_trigger", "Caused by:");
        provider.add("tooltip.gtceu.hazard_trigger.protection", "Protects from:");
        provider.add("tooltip.gtceu.hazard_trigger.inhalation", "Inhalation");
        provider.add("tooltip.gtceu.hazard_trigger.any", "Any contact");
        provider.add("tooltip.gtceu.hazard_trigger.skin_contact", "Skin contact");
        provider.add("tooltip.gtceu.hazard_trigger.none", "Nothing");

        // Antidote
        provider.add("gtceu.medical_condition.antidote.description", "§aAntidote §7Hold Shift to show details");
        provider.add("gtceu.medical_condition.antidote.description_shift", "§aCures these conditions:");
        provider.add("gtceu.medical_condition.antidote.description.effect_removed",
                "Removes %s%% of current conditions' effects");
        provider.add("tooltip.gtceu.antidote.description.effect_removed.all",
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
