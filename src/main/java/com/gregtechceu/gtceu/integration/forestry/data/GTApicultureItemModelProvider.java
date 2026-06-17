package com.gregtechceu.gtceu.integration.forestry.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
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
        ModelFile forestryCombModel = new ModelFile.UncheckedModelFile(new ResourceLocation(GTValues.MODID_FORESTRY, "item/bee_combs"));

        for (GTCombType type : GTCombType.VALUES) {
            getBuilder("bee_comb_" + type.getSerializedName())
                    .parent(forestryCombModel);
        }

        for (FeatureItem<ItemCrated> featureCrated : CrateItems.getCrates()) {
            Item containedItem = featureCrated.get().getContained().getItem();
            String id = featureCrated.getName();

            if (GTApicultureItems.BEE_COMBS.itemEqual(containedItem)) {
                filledCrateModelLayered(id, modLoc("item/bee_combs.0"), modLoc("item/bee_combs.1"));
            }
        }

        basicItem(GTApicultureItems.FRAME_ACCELERATED.get());
        basicItem(GTApicultureItems.FRAME_MUTAGENIC.get());
        basicItem(GTApicultureItems.FRAME_WORKING.get());
        basicItem(GTApicultureItems.FRAME_DECAYING.get());
        basicItem(GTApicultureItems.FRAME_SLOWING.get());
        basicItem(GTApicultureItems.FRAME_STABILIZING.get());
        basicItem(GTApicultureItems.FRAME_ARBORIST.get());

    }

    private void filledCrateModelLayered(String id, ResourceLocation layer1, ResourceLocation layer2) {
        getBuilder(id)
                .customLoader(FilledCrateModelBuilder::begin)
                .layer1(layer1)
                .layer2(layer2)
                .end();
    }
}
