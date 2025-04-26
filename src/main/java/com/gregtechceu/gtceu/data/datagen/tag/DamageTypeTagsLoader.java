package com.gregtechceu.gtceu.data.datagen.tag;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.data.damagesource.GTDamageTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DamageTypeTagsLoader extends DamageTypeTagsProvider {

    public DamageTypeTagsLoader(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, GTCEu.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(DamageTypeTags.IS_FIRE).add(GTDamageTypes.HEAT);
        this.tag(Tags.DamageTypes.IS_POISON).add(GTDamageTypes.CHEMICAL);
        this.tag(DamageTypeTags.IS_LIGHTNING).add(GTDamageTypes.ELECTRIC);
        this.tag(DamageTypeTags.WITHER_IMMUNE_TO).add(GTDamageTypes.RADIATION);

        this.tag(Tags.DamageTypes.IS_PHYSICAL)
                .add(GTDamageTypes.HEAT, GTDamageTypes.TURBINE);
        this.tag(Tags.DamageTypes.IS_ENVIRONMENT)
                .add(GTDamageTypes.RADIATION, GTDamageTypes.CHEMICAL, GTDamageTypes.ELECTRIC);

        var bypassesArmor = this.tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(GTDamageTypes.HEAT, GTDamageTypes.CHEMICAL, GTDamageTypes.RADIATION, GTDamageTypes.TURBINE);
        for (MedicalCondition condition : MedicalCondition.CONDITIONS.values()) {
            bypassesArmor.add(condition.getDamageType());
        }
    }
}
