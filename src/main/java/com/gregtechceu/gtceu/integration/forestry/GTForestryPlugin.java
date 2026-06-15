package com.gregtechceu.gtceu.integration.forestry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.forestry.bee.GTBeeDefinition;
import com.gregtechceu.gtceu.integration.forestry.bee.GTTaxa;
import forestry.api.client.IClientModuleHandler;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.IGeneticRegistration;
import net.minecraft.resources.ResourceLocation;


public class GTForestryPlugin implements IForestryPlugin {
    public static final ResourceLocation ID =  new ResourceLocation(GTCEu.MOD_ID, "core");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void registerApiculture(IApicultureRegistration apiculture) {
        GTBeeDefinition.register(apiculture);
    }




    @Override
    public void registerGenetics(IGeneticRegistration genetics) {
        GTTaxa.defineTaxa(genetics);
    }

}
