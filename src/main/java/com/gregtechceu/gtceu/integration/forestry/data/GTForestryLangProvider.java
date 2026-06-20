package com.gregtechceu.gtceu.integration.forestry.data;

import com.gregtechceu.gtceu.integration.forestry.bee.GTBeesSpecies;
import com.gregtechceu.gtceu.integration.forestry.items.GTApicultureItems;
import com.gregtechceu.gtceu.integration.forestry.items.GTCombType;

import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import java.lang.reflect.Field;

public class GTForestryLangProvider {

    public static void initGeneratedNames(RegistrateLangProvider provider) {
        provider.add("item.gtceu.bee.modifier.speed_multiplier", "Production");
        provider.add("item.gtceu.bee.modifier.decay_multiplier", "Decay");
        provider.add("item.gtceu.bee.modifier.pollination_multiplier", "Pollination");
        provider.add("item.gtceu.bee.modifier.mutation_multiplier", "Mutation");
        provider.add("item.gtceu.bee.modifier.aging_multiplier", "Aging");
        provider.add("item.gtceu.bee.modifier.is_rainproof", "Shields Rain");
        provider.add("item.gtceu.bee.modifier.is_always_sunny", "Simulates Sunlight");
        provider.add("item.gtceu.bee.modifier.is_hellish", "Simulates Nether");

        //todo If you use registrate, you won't need to do this manually.
        provider.add(GTApicultureItems.FRAME_ACCELERATED.get(), "Accelerated Frame");
        provider.add(GTApicultureItems.FRAME_MUTAGENIC.get(), "Mutagenic Frame");
        provider.add(GTApicultureItems.FRAME_WORKING.get(), "Working Frame");
        provider.add(GTApicultureItems.FRAME_DECAYING.get(), "Decaying Frame");
        provider.add(GTApicultureItems.FRAME_SLOWING.get(), "Slowing Frame");
        provider.add(GTApicultureItems.FRAME_STABILIZING.get(), "Stabilizing Frame");
        provider.add(GTApicultureItems.FRAME_ARBORIST.get(), "Arborist Frame");


        //todo use FormatingUtils
        for (GTCombType type : GTCombType.VALUES) {
            String material = type.getSerializedName();
            String materialCap = material.substring(0, 1).toUpperCase() + material.substring(1);
            provider.add("item.gtceu.bee_comb_" + material, materialCap + " " + "Comb");
        }


        //todo scrap this
        try {
            for (Field field : GTBeesSpecies.class.getDeclaredFields()) {
                if (field.getType() == ResourceLocation.class) {
                    //todo where is this used? If possible, you should make it use the materials' translations instead.
                    ResourceLocation location = (ResourceLocation) field.get(null);
                    String speciesName = location.getPath();
                    String speciesNameCap = speciesName.substring(0, 1).toUpperCase() + speciesName.substring(1);
                    provider.add("allele.forestry.bee_species.gtceu." + speciesName, speciesNameCap);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
