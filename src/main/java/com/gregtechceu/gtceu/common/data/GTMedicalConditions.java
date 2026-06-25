package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class GTMedicalConditions {

    private static final DeferredRegister<MedicalCondition> MEDICAL_CONDITION = DeferredRegister.create(GTRegistries.Keys.MEDICAL_CONDITION, "gtceu");


    // General Conditions
    public static final RegistryObject<MedicalCondition> NONE = MEDICAL_CONDITION.register("none", () -> new MedicalCondition(GTCEu.id("none"), 0xffffff, 0,
            MedicalCondition.IdleProgressionType.NONE, 0, false));

    // takes 5 minutes of having burn-causing items in the player's inventory for them to get the weakness effect
    // heals 2 seconds' worth of progression every second when not holding those items
    public static final RegistryObject<MedicalCondition> CHEMICAL_BURNS = MEDICAL_CONDITION.register("chemical_burns", () -> new MedicalCondition(GTCEu.id("chemical_burns"),0xbc305a, 300,
            MedicalCondition.IdleProgressionType.HEAL, 2, false,
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS))
            .setRecipeModifier(builder -> builder
                    .outputFluids(DilutedHydrochloricAcid.getFluid(500))
                    .outputFluids(DilutedSulfuricAcid.getFluid(750))));

    // takes 5 minutes of having poisonous items in the player's inventory for them to get the weakness effect
    // at 10 minutes, they get a weaker version of the poison effect
    // heals 2 seconds' worth of progression every second when not holding those items
    public static final RegistryObject<MedicalCondition> POISON = MEDICAL_CONDITION.register("poison",
            () -> new MedicalCondition(GTCEu.id("poison"), 0xA36300, 600,
            MedicalCondition.IdleProgressionType.HEAL, 2, true,
            new Symptom.ConfiguredSymptom(Symptom.WEAK_POISONING),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 300, 600))
            .setRecipeModifier(builder -> builder
                    .outputFluids(SulfurTrioxide.getFluid(1000))));

    // having weakly poisonous items in the player's inventory gives them the weak poison effect
    // the effect ramps up in 6 "stages", getting progressively worse every stage. It caps out at Weak Poison 10.
    // does NOT heal automatically
    public static final RegistryObject<MedicalCondition> WEAK_POISON = MEDICAL_CONDITION.register("weak_poison",
            () -> new MedicalCondition(GTCEu.id("weak_poison"), 0x6D7917, 1800,
            MedicalCondition.IdleProgressionType.NONE, 0, false,
            new Symptom.ConfiguredSymptom(Symptom.WEAK_POISONING, 6, 1800))
            .setRecipeModifier(builder -> builder
                    .outputFluids(NitricOxide.getFluid(1000))));

    // takes 2.5 minutes of having irritating items in the player's inventory for them to get the weakness effect
    // at 5 minutes, they begin getting random damage every so often
    // heals 5 seconds' worth of progression every second when not holding those items
    public static final RegistryObject<MedicalCondition> IRRITANT = MEDICAL_CONDITION.register("irritant",
            () -> new MedicalCondition(GTCEu.id("irritant"), 0x02512f, 600,
            MedicalCondition.IdleProgressionType.HEAL, 5, false,
            new Symptom.ConfiguredSymptom(Symptom.RANDOM_DAMAGE),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 300, 600))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, DarkAsh, 4)));

    // takes 5 minutes of having nauseating items in the player's inventory for them to get the nausea effect
    // heals 5 seconds' worth of progression every second when not holding those items
    public static final RegistryObject<MedicalCondition> NAUSEA = MEDICAL_CONDITION.register("nausea",
            () -> new MedicalCondition(GTCEu.id("nausea"), 0x1D4A00, 600,
            MedicalCondition.IdleProgressionType.HEAL, 5, false,
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA, 1, 420, 600))
            .setRecipeModifier(builder -> builder
                    .outputFluids(CarbonMonoxide.getFluid(50))));
    /**
     * All effects:
     * <ul>
     * <li>After an hour, the player's attack damage and mining speed will start lowering.</li>
     * <li>After 2 hours, the player's max air supply and movement speed will start lowering.</li>
     * <li>After 3 hours, the player's max health will start lowering.</li>
     * <li>After 4 hours, the player's attack damage and mining speed are at their lowest.</li>
     * <li>After 5 hours, the player will have the lowest max air supply of 200/300 (so 6/10 bubbles) and their speed is
     * at its lowest.</li>
     * <li>After 6 hours, the player will have the lowest max HP of 10/20 (so 5/10 hearts).</li>
     * <li>After 10 real-life hours (30 MC days) of having cancer, the player will die.</li>
     * </ul>
     * </p>
     * Do note that while the effects do not get worse when the player isn't holding anything radioactive,
     * the progression they've already gained will stay as is and reactivate if they pick up e.g. a uranium ingot.<br>
     *
     * This condition does <strong>NOT</strong> heal automatically. You can use {@linkplain GTItems#RAD_AWAY_PILL
     * rad-away pills} to heal it.
     */
    public static final RegistryObject<MedicalCondition> CARCINOGEN = MEDICAL_CONDITION.register("carcinogen",
            () -> new MedicalCondition(GTCEu.id("carcinogen"), 0x0f570f, 36000,
            MedicalCondition.IdleProgressionType.NONE, 0, true,
            new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, 10800, 21600),
            new Symptom.ConfiguredSymptom(Symptom.AIR_SUPPLY_DEBUFF, 7200, 18000),
            new Symptom.ConfiguredSymptom(Symptom.MINING_FATIGUE, 3600, 14400),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 7200, 18000),
            // new Symptom.ConfiguredSymptom(Symptom.HUNGER, 0, 10800),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 3600, 14400)));

    // Material specific Conditions
    public static final RegistryObject<MedicalCondition> ASBESTOSIS = MEDICAL_CONDITION.register("asbestosis",
            () -> new MedicalCondition(GTCEu.id("asbestosis"), 0xe3e3e3, 5000,
            MedicalCondition.IdleProgressionType.HEAL, 1, true,
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 1200, 5000))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, Asbestos, 4)));

    public static final RegistryObject<MedicalCondition> ARSENICOSIS = MEDICAL_CONDITION.register("arsenicosis",
            () -> new MedicalCondition(GTCEu.id("arsenicosis"), 0xbd4b15, 1000,
            MedicalCondition.IdleProgressionType.HEAL, 1, true,
            new Symptom.ConfiguredSymptom(Symptom.WITHER),
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 2, 500, 1000),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, 330, 1000))
            // new Symptom.ConfiguredSymptom(Symptom.HUNGER, 2, .2f))
            .setRecipeModifier(builder -> builder
                    .outputItems(dust, Arsenic, 4)));

    public static final RegistryObject<MedicalCondition> METHANOL_POISONING = MEDICAL_CONDITION.register("methanol_poisoning",
            () -> new MedicalCondition(GTCEu.id("methanol_poisoning"), 0xaa8800, 600,
            MedicalCondition.IdleProgressionType.HEAL, .5f, true,
            new Symptom.ConfiguredSymptom(Symptom.POISONING),
            new Symptom.ConfiguredSymptom(Symptom.BLINDNESS, 2, 450, 600),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, 300, 600),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 1, 150, 600))
            .setRecipeModifier(builder -> builder
                    .outputFluids(Methanol.getFluid(1000))));

    public static final RegistryObject<MedicalCondition> CARBON_MONOXIDE_POISONING = MEDICAL_CONDITION.register("carbon_monoxide_poisoning",
            () -> new MedicalCondition(GTCEu.id("carbon_monoxide_poisoning"),0x041525, 2000,
                    MedicalCondition.IdleProgressionType.HEAL, 1, true, new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 2, 1500, 2000),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 2, 500, 2000)
    ));

    public static void init(IEventBus modBus) {
        MEDICAL_CONDITION.register(modBus);
    }
}
