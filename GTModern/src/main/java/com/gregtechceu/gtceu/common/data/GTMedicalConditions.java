package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class GTMedicalConditions {

    // General Conditions
    public static final MedicalCondition NONE = new MedicalCondition("none", 0xffffff, 0);
    public static final MedicalCondition CHEMICAL_BURNS = new MedicalCondition("chemical_burns", 0xbc305a, 200,
            MedicalCondition.IdleProgressionType.HEAL, 2, false,
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS))
            .setRecipeModifier(builder -> builder
                    .outputFluids(DilutedHydrochloricAcid.getFluid(500))
                    .outputFluids(DilutedSulfuricAcid.getFluid(750)));
    public static final MedicalCondition POISON = new MedicalCondition("poison", 0xA36300, 600,
            MedicalCondition.IdleProgressionType.HEAL, 2, true,
            new Symptom.ConfiguredSymptom(Symptom.WEAK_POISONING),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, .5f))
            .setRecipeModifier(builder -> builder
                    .outputFluids(SulfurTrioxide.getFluid(1000)));
    public static final MedicalCondition WEAK_POISON = new MedicalCondition("weak_poison", 0x6D7917, 3000,
            MedicalCondition.IdleProgressionType.NONE, 0, false,
            new Symptom.ConfiguredSymptom(Symptom.WEAK_POISONING, 6, .3f))
            .setRecipeModifier(builder -> builder
                    .outputFluids(NitricOxide.getFluid(1000)));
    public static final MedicalCondition IRRITANT = new MedicalCondition("irritant", 0x02512f, 600,
            MedicalCondition.IdleProgressionType.HEAL, 5, false,
            new Symptom.ConfiguredSymptom(Symptom.RANDOM_DAMAGE),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 0.5f))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, DarkAsh, 4));
    public static final MedicalCondition NAUSEA = new MedicalCondition("nausea", 0x1D4A00, 600,
            MedicalCondition.IdleProgressionType.HEAL, 5, false,
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA))
            .setRecipeModifier(builder -> builder
                    .outputFluids(CarbonMonoxide.getFluid(50)));
    public static final MedicalCondition CARCINOGEN = new MedicalCondition("carcinogen", 0x0f570f, 20000,
            MedicalCondition.IdleProgressionType.NONE, 0, true,
            new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, .75f),
            new Symptom.ConfiguredSymptom(Symptom.AIR_SUPPLY_DEBUFF, .5f),
            new Symptom.ConfiguredSymptom(Symptom.MINING_FATIGUE, .4f),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, .4f),
            new Symptom.ConfiguredSymptom(Symptom.HUNGER, .3f),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, .2f));

    // Material specific Conditions
    public static final MedicalCondition ASBESTOSIS = new MedicalCondition("asbestosis", 0xe3e3e3, 5000,
            MedicalCondition.IdleProgressionType.HEAL, 1, true,
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, .6f),
            new Symptom.ConfiguredSymptom(Symptom.AIR_SUPPLY_DEBUFF, .3f),
            new Symptom.ConfiguredSymptom(Symptom.HUNGER, .2f),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, .2f))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, Asbestos, 4));
    public static final MedicalCondition ARSENICOSIS = new MedicalCondition("arsenicosis", 0xbd4b15, 1000,
            MedicalCondition.IdleProgressionType.HEAL, 1, true,
            new Symptom.ConfiguredSymptom(Symptom.WITHER),
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, .6f),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 2, .5f),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, .3f),
            new Symptom.ConfiguredSymptom(Symptom.HUNGER, 2, .2f),
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA, .1f))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, Arsenic, 4));
    public static final MedicalCondition SILICOSIS = new MedicalCondition("silicosis", 0x5d6c91, 15000,
            MedicalCondition.IdleProgressionType.HEAL, .5f, true,
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, 4, .75f),
            new Symptom.ConfiguredSymptom(Symptom.AIR_SUPPLY_DEBUFF, .6f))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, SiliconDioxide, 4));
    public static final MedicalCondition BERYLLIOSIS = new MedicalCondition("berylliosis", 0x0c6539, 10000,
            MedicalCondition.IdleProgressionType.HEAL, .5f, true,
            new Symptom.ConfiguredSymptom(Symptom.WITHER),
            new Symptom.ConfiguredSymptom(Symptom.RANDOM_DAMAGE, 1, .7f),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 2, .5f),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, .4f))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, Beryllium, 4));
    public static final MedicalCondition METHANOL_POISONING = new MedicalCondition("methanol_poisoning", 0xaa8800, 500,
            MedicalCondition.IdleProgressionType.HEAL, .5f, true,
            new Symptom.ConfiguredSymptom(Symptom.POISONING),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 1, .75f),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, .5f),
            new Symptom.ConfiguredSymptom(Symptom.BLINDNESS, 2, .25f))
            .setRecipeModifier(builder -> builder
                    .outputFluids(Methanol.getFluid(1000)));
    public static final MedicalCondition CARBON_MONOXIDE_POISONING = new MedicalCondition("carbon_monoxide_poisoning",
            0x041525, 2000, MedicalCondition.IdleProgressionType.HEAL, 1, true,
            new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 2, .75f),
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA, 2, .5f),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, .25f))
            .setRecipeModifier(builder -> builder
                    .outputFluids(CarbonMonoxide.getFluid(1000)));
}
