package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.resources.Identifier;

import com.google.gson.JsonObject;

public class BlockModelBuilder extends ModelBuilder<BlockModelBuilder> {

    public BlockModelBuilder(Identifier outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
    }

    @Override
    public JsonObject toJson() {
        return super.toJson();
    }
}
