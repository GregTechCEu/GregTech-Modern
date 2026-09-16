package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;

import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTMedicalConditions {

    // spotless:off

    // General Conditions
    public static final DeferredHolder<MedicalCondition, MedicalCondition> NONE = REGISTRATE.medicalCondition("none")
            .lang("Not Dangerous", "Nothing?")
            .register();

    // takes 5 minutes of having burn-causing items in the player's inventory for them to get the weakness effect
    // heals 2 seconds' worth of progression every second when not holding those items
    public static final RegistryEntry<MedicalCondition, MedicalCondition> CHEMICAL_BURNS = REGISTRATE.medicalCondition("chemical_burns")
            .lang("Chemical Burns")
            .color(0xbc305a).maxProgression(300).progressionRate(2).progressionType(MedicalCondition.IdleProgressionType.HEAL)
            .recipeModifier(builder -> builder
                    .outputFluids(DilutedHydrochloricAcid.getFluid(500))
                    .outputFluids(DilutedSulfuricAcid.getFluid(750)))
            .symptom(Symptom.WEAKNESS)
            .register();

    // takes 5 minutes of having poisonous items in the player's inventory for them to get the weakness effect
    // at 10 minutes, they get a weaker version of the poison effect
    // heals 2 seconds' worth of progression every second when not holding those items
    public static final RegistryEntry<MedicalCondition, MedicalCondition> POISON = REGISTRATE.medicalCondition("poison")
            .lang("Poisonous", "Poisoning")
            .color(0xA36300).maxProgression(600).progressionRate(2).canBePermenant(true)
            .progressionType(MedicalCondition.IdleProgressionType.HEAL)
            .recipeModifier(builder -> builder.outputFluids(SulfurTrioxide.getFluid(1000)))
            .symptom(Symptom.WEAK_POISONING)
            .symptom(Symptom.WEAKNESS, 300, 600)
            .register();

    // having weakly poisonous items in the player's inventory gives them the weak poison effect
    // the effect ramps up in 6 "stages", getting progressively worse every stage. It caps out at Weak Poison 10.
    // does NOT heal automatically
    public static final RegistryEntry<MedicalCondition, MedicalCondition> WEAK_POISON = REGISTRATE.medicalCondition("weak_poison")
            .lang("Weakly Poisonous", "Minor poisoning")
            .color(0x6D7917).maxProgression(1800).progressionRate(0)
            .recipeModifier(builder -> builder.outputFluids(NitricOxide.getFluid(1000)))
            .symptom(Symptom.WEAK_POISONING, 6, 1800)
            .register();

    // takes 2.5 minutes of having irritating items in the player's inventory for them to get the weakness effect
    // at 5 minutes, they begin getting random damage every so often
    // heals 5 seconds' worth of progression every second when not holding those items
    public static final RegistryEntry<MedicalCondition, MedicalCondition> IRRITANT = REGISTRATE.medicalCondition("irritant")
            .lang("Irritant", "Irritation")
            .color(0x02512f).maxProgression(600).progressionRate(5)
            .progressionType(MedicalCondition.IdleProgressionType.HEAL)
            .recipeModifier(builder -> builder.outputItems(dust, DarkAsh, 4))
            .symptom(Symptom.RANDOM_DAMAGE)
            .symptom(Symptom.WEAKNESS, 300, 600)
            .register();

    // takes 5 minutes of having nauseating items in the player's inventory for them to get the nausea effect
    // heals 5 seconds' worth of progression every second when not holding those items
    public static final RegistryEntry<MedicalCondition, MedicalCondition> NAUSEA = REGISTRATE.medicalCondition("nausea")
            .lang("Nauseating", "Nausea")
            .color(0x1D4A00).maxProgression(600).progressionRate(5)
            .progressionType(MedicalCondition.IdleProgressionType.HEAL)
            .recipeModifier(builder -> builder.outputFluids(CarbonMonoxide.getFluid(50)))
            .symptom(Symptom.NAUSEA, 1, 420, 600)
            .register();

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
    public static final DeferredHolder<MedicalCondition, MedicalCondition> CARCINOGEN = REGISTRATE.medicalCondition("carcinogen")
            .lang("Carcinogenic", "Cancer")
            .color(0x0f570f).maxProgression(36000).progressionRate(0).canBePermenant(true)
            .symptom(Symptom.DEATH)
            .symptom(Symptom.HEALTH_DEBUFF, 10800, 21600)
            .symptom(Symptom.AIR_SUPPLY_DEBUFF, 7200, 18000)
            .symptom(Symptom.MINING_FATIGUE, 3600, 14400)
            .symptom(Symptom.SLOWNESS, 7200, 18000)
            .symptom(Symptom.WEAKNESS, 3600, 14400)
            .register();

    // Material specific Conditions
    public static final DeferredHolder<MedicalCondition, MedicalCondition> ASBESTOSIS = REGISTRATE.medicalCondition("asbestosis")
            .lang("Asbestosis")
            .color(0xe3e3e3).maxProgression(5000).progressionRate(1).canBePermenant(true)
            .progressionType(MedicalCondition.IdleProgressionType.HEAL)
            .recipeModifier(builder -> builder.outputItems(dust, Asbestos, 4))
            .symptom(Symptom.WEAKNESS, 1200, 5000)
            .register();

    public static final DeferredHolder<MedicalCondition, MedicalCondition> ARSENICOSIS = REGISTRATE.medicalCondition("arsenicosis")
            .lang("Arsenicosis")
            .color(0xbd4b15).maxProgression(1000).progressionRate(1).canBePermenant(true)
            .progressionType(MedicalCondition.IdleProgressionType.HEAL)
            .recipeModifier(builder -> builder.outputItems(dust, Arsenic, 4))
            .symptom(Symptom.WITHER)
            .symptom(Symptom.NAUSEA)
            .symptom(Symptom.SLOWNESS, 2, 500, 1000)
            .symptom(Symptom.WEAKNESS, 2, 330, 1000)
            .register();

    public static final DeferredHolder<MedicalCondition, MedicalCondition> METHANOL_POISONING = REGISTRATE.medicalCondition("methanol_poisoning")
            .lang("Methanol Poisoning")
            .color(0xaa8800).maxProgression(600).progressionRate(0.5f).canBePermenant(true)
            .progressionType(MedicalCondition.IdleProgressionType.HEAL)
            .recipeModifier(builder -> builder.outputFluids(Methanol.getFluid(1000)))
            .symptom(Symptom.POISONING)
            .symptom(Symptom.BLINDNESS, 2, 450, 600)
            .symptom(Symptom.WEAKNESS, 2, 300, 600)
            .symptom(Symptom.SLOWNESS, 1, 150, 600)
            .register();

    public static final DeferredHolder<MedicalCondition, MedicalCondition> CARBON_MONOXIDE_POISONING = REGISTRATE.medicalCondition("carbon_monoxide_poisoning")
            .lang("Carbon Monoxide Poisoning")
            .color(0x041525).maxProgression(200).progressionRate(1).canBePermenant(true)
            .progressionType(MedicalCondition.IdleProgressionType.HEAL)
            .recipeModifier(builder -> builder.outputFluids(CarbonMonoxide.getFluid(1000)))
            .symptom(Symptom.DEATH)
            .symptom(Symptom.NAUSEA)
            .symptom(Symptom.SLOWNESS, 2, 1500, 2000)
            .symptom(Symptom.WEAKNESS, 2, 500, 2000)
            .register();
    // spotless:on

    public static void init() {}
}
