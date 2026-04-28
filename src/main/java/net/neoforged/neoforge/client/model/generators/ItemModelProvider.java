package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public abstract class ItemModelProvider extends ModelProvider<ItemModelBuilder> {

    public ItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, ITEM_FOLDER, ItemModelBuilder::new, existingFileHelper);
    }

    public ItemModelBuilder generated(String name, Identifier layer0) {
        return getBuilder(name).parent(new ModelFile.UncheckedModelFile("item/generated")).texture("layer0", layer0);
    }

    @Override
    public String getName() {
        return "Item Models: " + modid;
    }
}
