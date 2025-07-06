package com.gregtechceu.gtceu.data.model;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelProvider;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

public class BlockstateModelLoader {

    public static void init(GTBlockstateProvider provider) {
        BlockModelProvider models = provider.models();

        // tiered hulls
        for (int tier : GTValues.ALL_TIERS) {
            ResourceLocation modelName = TIERED_HULL_MODELS.get(tier);

            var model = models.withExistingParent(modelName.toString(), SIDED_SIDED_OVERLAY_MODEL);
            GTMachineModels.casingTextures(model, modelName);
        }
        // steam hulls
        {
            ResourceLocation modelName = LP_STEAM_HULL_MODEL;
            var model = models.withExistingParent(modelName.toString(), SIDED_SIDED_OVERLAY_MODEL);
            GTMachineModels.casingTextures(model, modelName);

            modelName = HP_STEAM_HULL_MODEL;
            model = models.withExistingParent(modelName.toString(), SIDED_SIDED_OVERLAY_MODEL);
            GTMachineModels.casingTextures(model, modelName);
        }
    }
}
