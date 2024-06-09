package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class ToolLang {

    public static void init(RegistrateLangProvider provider) {
        initDeathMessages(provider);
        initToolInfo(provider);
    }

    private static void initDeathMessages(RegistrateLangProvider provider) {
        provider.add("death.attack.gtceu.heat", "%s was boiled alive");
        provider.add("death.attack.gtceu.frost", "%s explored cryogenics");
        provider.add("death.attack.gtceu.chemical", "%s had a chemical accident");
        provider.add("death.attack.gtceu.electric", "%s was electrocuted");
        provider.add("death.attack.gtceu.radiation", "%s glows with joy now");
        provider.add("death.attack.gtceu.turbine", "%s put their head into a turbine");
        provider.add("death.attack.gtceu.explosion", "%s exploded");
        provider.add("death.attack.gtceu.explosion.player", "%s exploded with help of %s");
        provider.add("death.attack.gtceu.heat.player", "%s was boiled alive by %s");
        provider.add("death.attack.gtceu.pickaxe", "%s got mined by %s");
        provider.add("death.attack.gtceu.shovel", "%s got dug up by %s");
        provider.add("death.attack.gtceu.axe", "%s has been chopped by %s");
        provider.add("death.attack.gtceu.hoe", "%s had their head tilled by %s");
        provider.add("death.attack.gtceu.hammer", "%s was squashed by %s");
        provider.add("death.attack.gtceu.mallet", "%s got hammered to death by %s");
        provider.add("death.attack.gtceu.mining_hammer", "%s was mistaken for Ore by %s");
        provider.add("death.attack.gtceu.spade", "%s got excavated by %s");
        provider.add("death.attack.gtceu.wrench", "%s gave %s a whack with the Wrench!");
        provider.add("death.attack.gtceu.file", "%s has been filed D for 'Dead' by %s");
        provider.add("death.attack.gtceu.crowbar", "%s lost half a life to %s");
        provider.add("death.attack.gtceu.screwdriver", "%s has screwed with %s for the last time!");
        provider.add("death.attack.gtceu.mortar", "%s was ground to dust by %s");
        provider.add("death.attack.gtceu.wire_cutter", "%s has cut the cable for the Life Support Machine of %s");
        provider.add("death.attack.gtceu.scythe", "%s had their soul taken by %s");
        provider.add("death.attack.gtceu.knife", "%s was gently poked by %s");
        provider.add("death.attack.gtceu.butchery_knife", "%s was butchered by %s");
        provider.add("death.attack.gtceu.drill_lv", "%s was drilled with 32V by %s");
        provider.add("death.attack.gtceu.drill_mv", "%s was drilled with 128V by %s");
        provider.add("death.attack.gtceu.drill_hv", "%s was drilled with 512V by %s");
        provider.add("death.attack.gtceu.drill_ev", "%s was drilled with 2048V by %s");
        provider.add("death.attack.gtceu.drill_iv", "%s was drilled with 8192V by %s");
        provider.add("death.attack.gtceu.chainsaw_lv", "%s was massacred by %s");
        provider.add("death.attack.gtceu.wrench_lv", "%s's pipes were loosened by %s");
        provider.add("death.attack.gtceu.wrench_hv", "%s's pipes were loosened by %s");
        provider.add("death.attack.gtceu.wrench_iv", "%s had a Monkey Wrench thrown into their plans by %s");
        provider.add("death.attack.gtceu.buzzsaw", "%s got buzzed by %s");
        provider.add("death.attack.gtceu.screwdriver_lv", "%s had their screws removed by %s");
    }

    private static void initToolInfo(RegistrateLangProvider provider) {}
}
