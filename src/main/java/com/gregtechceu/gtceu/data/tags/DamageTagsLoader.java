package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.damagesource.DamageTypeData;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DamageTagsLoader extends TagsProvider<DamageType> {

    public DamageTagsLoader(PackOutput arg, CompletableFuture<HolderLookup.Provider> completableFuture,
                            @Nullable ExistingFileHelper existingFileHelper) {
        super(arg, Registries.DAMAGE_TYPE, completableFuture, GTCEu.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        DamageTypeData.allInNamespace(GTCEu.MOD_ID).forEach(damageTypeData -> damageTypeData.tags
                .forEach(damageTypeTagKey -> tag(damageTypeTagKey).add(damageTypeData.key)));
    }
}
