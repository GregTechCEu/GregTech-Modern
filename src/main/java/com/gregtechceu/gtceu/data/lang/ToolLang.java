package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.tterrag.registrate.providers.RegistrateLangProvider;

public class ToolLang {

    public static void init(RegistrateLangProvider provider) {
        initDeathMessages(provider);
        initToolInfo(provider);
    }

    private static void initDeathMessages(RegistrateLangProvider provider) {
        provider.add("death.attack.heat", "%s was boiled alive");
        provider.add("death.attack.frost", "%s explored cryogenics");
        provider.add("death.attack.chemical", "%s had a chemical accident");
        provider.add("death.attack.electric", "%s was electrocuted");
        provider.add("death.attack.radiation", "%s glows with joy now");
        provider.add("death.attack.turbine", "%s put their head into a turbine");
        provider.add("death.attack.explosion", "%s exploded");
        provider.add("death.attack.explosion.player", "%s exploded with help of %s");
        provider.add("death.attack.heat.player", "%s was boiled alive by %s");
        provider.add("death.attack.pickaxe", "%s got mined by %s");
        provider.add("death.attack.shovel", "%s got dug up by %s");
        provider.add("death.attack.axe", "%s has been chopped by %s");
        provider.add("death.attack.hoe", "%s had their head tilled by %s");
        provider.add("death.attack.hammer", "%s was squashed by %s");
        provider.add("death.attack.mallet", "%s got hammered to death by %s");
        provider.add("death.attack.mining_hammer", "%s was mistaken for Ore by %s");
        provider.add("death.attack.spade", "%s got excavated by %s");
        provider.add("death.attack.wrench", "%s gave %s a whack with the Wrench!");
        provider.add("death.attack.file", "%s has been filed D for 'Dead' by %s");
        provider.add("death.attack.crowbar", "%s lost half a life to %s");
        provider.add("death.attack.screwdriver", "%s has screwed with %s for the last time!");
        provider.add("death.attack.mortar", "%s was ground to dust by %s");
        provider.add("death.attack.wire_cutter", "%s has cut the cable for the Life Support Machine of %s");
        provider.add("death.attack.scythe", "%s had their soul taken by %s");
        provider.add("death.attack.knife", "%s was gently poked by %s");
        provider.add("death.attack.butchery_knife", "%s was butchered by %s");
        provider.add("death.attack.drill_lv", "%s was drilled with 32V by %s");
        provider.add("death.attack.drill_mv", "%s was drilled with 128V by %s");
        provider.add("death.attack.drill_hv", "%s was drilled with 512V by %s");
        provider.add("death.attack.drill_ev", "%s was drilled with 2048V by %s");
        provider.add("death.attack.drill_iv", "%s was drilled with 8192V by %s");
        provider.add("death.attack.chainsaw_lv", "%s was massacred by %s");
        provider.add("death.attack.wrench_lv", "%s's pipes were loosened by %s");
        provider.add("death.attack.wrench_hv", "%s's pipes were loosened by %s");
        provider.add("death.attack.wrench_iv", "%s had a Monkey Wrench thrown into their plans by %s");
        provider.add("death.attack.buzzsaw", "%s got buzzed by %s");
        provider.add("death.attack.screwdriver_lv", "%s had their screws removed by %s");
    }

    private static void initToolInfo(RegistrateLangProvider provider) {

    }
}
