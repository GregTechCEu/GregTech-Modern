package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class EntityTypeTagLoader extends EntityTypeTagsProvider {

    public EntityTypeTagLoader(PackOutput arg, CompletableFuture<HolderLookup.Provider> completableFuture, @Nullable ExistingFileHelper existingFileHelper) {
        super(arg, completableFuture, GTCEu.MOD_ID, existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(CustomTags.HEAT_IMMUNE).add(EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.WITHER_SKELETON, EntityType.WITHER);
        tag(CustomTags.CHEMICAL_IMMUNE).add(EntityType.SKELETON, EntityType.STRAY);
    }

}
