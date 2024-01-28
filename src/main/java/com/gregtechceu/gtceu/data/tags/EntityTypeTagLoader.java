package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class EntityTypeTagLoader extends EntityTypeTagsProvider {

    public EntityTypeTagLoader(DataGenerator arg, @Nullable ExistingFileHelper existingFileHelper) {
        super(arg, GTCEu.MOD_ID, existingFileHelper);
    }

    @Override
    public void addTags() {
        tag(CustomTags.HEAT_IMMUNE).add(EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.WITHER_SKELETON, EntityType.WITHER);
        tag(CustomTags.CHEMICAL_IMMUNE).add(EntityType.SKELETON, EntityType.STRAY);
    }

}
