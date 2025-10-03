package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoader;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class GTMedicalConditions {

    static {
        GTRegistries.MEDICAL_CONDITIONS.unfreeze();
    }

    // General Conditions
    public static final MedicalCondition NONE = register("none", 0xffffff, 0,
            MedicalCondition.IdleProgressionType.NONE, 0, false);
    public static final MedicalCondition CHEMICAL_BURNS = register("chemical_burns", 0xbc305a, 300,
            MedicalCondition.IdleProgressionType.HEAL, 2, false,
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS))
            .setRecipeModifier(builder -> builder
                    .outputFluids(DilutedHydrochloricAcid.getFluid(500))
                    .outputFluids(DilutedSulfuricAcid.getFluid(750)));
    public static final MedicalCondition POISON = register("poison", 0xA36300, 600,
            MedicalCondition.IdleProgressionType.HEAL, 2, true,
            new Symptom.ConfiguredSymptom(Symptom.WEAK_POISONING),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 300, 600))
            .setRecipeModifier(builder -> builder
                    .outputFluids(SulfurTrioxide.getFluid(1000)));
    public static final MedicalCondition WEAK_POISON = register("weak_poison", 0x6D7917, 1800,
            MedicalCondition.IdleProgressionType.NONE, 0, false,
            new Symptom.ConfiguredSymptom(Symptom.WEAK_POISONING, 6, 1800))
            .setRecipeModifier(builder -> builder
                    .outputFluids(NitricOxide.getFluid(1000)));
    public static final MedicalCondition IRRITANT = register("irritant", 0x02512f, 600,
            MedicalCondition.IdleProgressionType.HEAL, 5, false,
            new Symptom.ConfiguredSymptom(Symptom.RANDOM_DAMAGE),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 300, 600))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, DarkAsh, 4));
    public static final MedicalCondition NAUSEA = register("nausea", 0x1D4A00, 600,
            MedicalCondition.IdleProgressionType.HEAL, 5, false,
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA, 1, 420, 600))
            .setRecipeModifier(builder -> builder
                    .outputFluids(CarbonMonoxide.getFluid(50)));
    public static final MedicalCondition CARCINOGEN = register("carcinogen", 0x0f570f, 20000,
            MedicalCondition.IdleProgressionType.NONE, 0, true,
            new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, 10800, 21600),
            new Symptom.ConfiguredSymptom(Symptom.AIR_SUPPLY_DEBUFF, 7200, 18000),
            new Symptom.ConfiguredSymptom(Symptom.MINING_FATIGUE, 3600, 14400),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 7200, 18000),
            // new Symptom.ConfiguredSymptom(Symptom.HUNGER, 0, 10800),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 3600, 14400));

    // Material specific Conditions
    public static final MedicalCondition ASBESTOSIS = register("asbestosis", 0xe3e3e3, 5000,
            MedicalCondition.IdleProgressionType.HEAL, 1, true,
            // new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, 3000, 5000),
            // new Symptom.ConfiguredSymptom(Symptom.AIR_SUPPLY_DEBUFF, 1500, 3500),
            // new Symptom.ConfiguredSymptom(Symptom.HUNGER, 500, 4000),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 1200, 5000))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, Asbestos, 4));
    public static final MedicalCondition ARSENICOSIS = register("arsenicosis", 0xbd4b15, 1000,
            MedicalCondition.IdleProgressionType.HEAL, 1, true,
            new Symptom.ConfiguredSymptom(Symptom.WITHER),
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 2, 500, 1000),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, 330, 1000))
            //new Symptom.ConfiguredSymptom(Symptom.HUNGER, 2, .2f))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, Arsenic, 4));
    public static final MedicalCondition METHANOL_POISONING = register("methanol_poisoning", 0xaa8800, 600,
            MedicalCondition.IdleProgressionType.HEAL, .5f, true,
            new Symptom.ConfiguredSymptom(Symptom.POISONING),
            new Symptom.ConfiguredSymptom(Symptom.BLINDNESS, 2, 450, 600),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, 300, 600),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 1, 150, 600))
            .setRecipeModifier(builder -> builder
                    .outputFluids(Methanol.getFluid(1000)));
    public static final MedicalCondition CARBON_MONOXIDE_POISONING = register("carbon_monoxide_poisoning",
            0x041525, 2000, MedicalCondition.IdleProgressionType.HEAL, 1, true,
            new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 2, 1500, 2000),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, 500, 2000))
            .setRecipeModifier(builder -> builder
                    .outputFluids(CarbonMonoxide.getFluid(1000)));

    public static MedicalCondition register(ResourceLocation id, int color,
                                            int maxProgression, MedicalCondition.IdleProgressionType progressionType,
                                            float progressionRate, boolean canBePermanent,
                                            Symptom.ConfiguredSymptom... symptoms) {
        var condition = new MedicalCondition(id, color, maxProgression,
                progressionType, progressionRate, canBePermanent, symptoms);
        GTRegistries.MEDICAL_CONDITIONS.register(id, condition);
        return condition;
    }

    // internal variant of the above that skips having to write `register(GTCEu.id(...`
    private static MedicalCondition register(String id, int color,
                                             int maxProgression, MedicalCondition.IdleProgressionType progressionType,
                                             float progressionRate, boolean canBePermanent,
                                             Symptom.ConfiguredSymptom... symptoms) {
        return register(GTCEu.id(id), color, maxProgression, progressionType, progressionRate, canBePermanent,
                symptoms);
    }

    public static void init() {
        ModLoader.get()
                .postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.MEDICAL_CONDITIONS, MedicalCondition.class));
        GTRegistries.MEDICAL_CONDITIONS.freeze();
    }
}
