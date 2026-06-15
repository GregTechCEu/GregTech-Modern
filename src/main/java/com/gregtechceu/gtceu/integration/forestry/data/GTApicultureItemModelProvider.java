package com.gregtechceu.gtceu.integration.forestry.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.forestry.items.GTCombType;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import com.gregtechceu.gtceu.integration.forestry.items.GTApicultureItems;
import forestry.core.data.builder.FilledCrateModelBuilder;
import forestry.modules.features.FeatureItem;
import forestry.storage.features.CrateItems;
import forestry.storage.items.ItemCrated;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GTApicultureItemModelProvider extends ItemModelProvider {

    public GTApicultureItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, GTCEu.MOD_ID,existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModelFile forestryCombModel = new ModelFile.UncheckedModelFile(
                new ResourceLocation("forestry", "item/bee_combs")
        );

        for (GTCombType type : GTCombType.VALUES) {
            getBuilder("bee_comb_" + type.getSerializedName())
                    .parent(forestryCombModel);
        }

        // Crate models
        for (FeatureItem<ItemCrated> featureCrated : CrateItems.getCrates()) {
            Item containedItem = featureCrated.get().getContained().getItem();
            String id = featureCrated.getName();

            if (GTApicultureItems.BEE_COMBS.itemEqual(containedItem)) {
                filledCrateModelLayered(id,
                        new ResourceLocation("forestry", "item/bee_combs.0"),
                        new ResourceLocation("forestry", "item/bee_combs.1"));
            }
        }
    }

    private void filledCrateModelLayered(String id, ResourceLocation layer1, ResourceLocation layer2) {
        getBuilder(id)
                .customLoader(FilledCrateModelBuilder::begin)
                .layer1(layer1)
                .layer2(layer2)
                .end();
    }
}
