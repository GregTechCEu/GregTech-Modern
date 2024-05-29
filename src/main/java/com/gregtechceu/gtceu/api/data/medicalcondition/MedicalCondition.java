package com.gregtechceu.gtceu.api.data.medicalcondition;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import java.util.HashSet;

public class MedicalCondition {
    public final String name;
    public final int maxProgression;
    public final HashSet<Symptom.ConfiguredSymptom> symptoms = new HashSet<>();
    public final ResourceKey<DamageType> damageTypeKey;

    public MedicalCondition(String name, int maxProgression) {
        this.name = name;
        this.maxProgression = maxProgression;
        this.damageTypeKey = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(GTCEu.MOD_ID,name));
    }
}
