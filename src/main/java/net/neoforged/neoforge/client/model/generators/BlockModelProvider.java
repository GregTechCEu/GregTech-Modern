package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.data.PackOutput;

public abstract class BlockModelProvider extends ModelProvider<BlockModelBuilder> {

    public BlockModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, BLOCK_FOLDER, BlockModelBuilder::new, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Block Models: " + modid;
    }
}
