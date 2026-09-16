package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.providers.ProviderType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true)
public class MedicalConditionBuilder<P> extends AbstractBuilder<MedicalCondition, MedicalCondition, P, MedicalConditionBuilder<P>> {

    @Getter
    @Setter
    private int color = 0xffffff;

    @Getter
    @Setter
    private int maxProgression = 0;

    @Getter
    @Setter
    private MedicalCondition.IdleProgressionType progressionType = MedicalCondition.IdleProgressionType.NONE;

    @Getter
    @Setter
    private float progressionRate = 0f;

    @Getter
    @Setter
    private boolean canBePermenant = false;

    @Getter
    @Setter
    private Consumer<GTRecipeBuilder> recipeModifier = $ -> {};

    @Getter
    private List<Symptom.ConfiguredSymptom> symptoms = new ArrayList<>();


    public MedicalConditionBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback) {
        super(owner, parent, name, callback, GTRegistries.Keys.MEDICAL_CONDITION);
        this.defaultLang();
    }

    public MedicalConditionBuilder<P> defaultLang() {
        return super.lang(v -> v.id.toLanguageKey("medical_condition"));
    }

    public MedicalConditionBuilder<P> lang(String name) {
        return super.lang(v -> v.id.toLanguageKey("medical_condition"), name);
    }

    public MedicalConditionBuilder<P> lang(String name, String affectedLang) {
        return setData(ProviderType.LANG, (ctx, prov) -> {
            prov.add(ctx.getId().toLanguageKey("medical_condition"), name);
            prov.add(ctx.getId().toLanguageKey("medical_condition") + MedicalCondition.AFFECTED_SUFFIX, affectedLang);
        });
    }

    @Tolerate
    public MedicalConditionBuilder<P> symptoms(Symptom.ConfiguredSymptom... symptoms) {
        this.symptoms = Arrays.asList(symptoms);
        return this;
    }

    public MedicalConditionBuilder<P> symptom(Symptom.ConfiguredSymptom symptom) {
        symptoms.add(symptom);
        return this;
    }

    public MedicalConditionBuilder<P> symptom(Symptom symptom, int stages, int absMinThreshold, int absMaxThreshold) {
        return symptom(new Symptom.ConfiguredSymptom(symptom, stages, absMinThreshold, absMaxThreshold));
    }

    public MedicalConditionBuilder<P> symptom(Symptom symptom, int absMinThreshold, int absMaxThreshold) {
        return symptom(new Symptom.ConfiguredSymptom(symptom, absMinThreshold, absMaxThreshold));
    }

    public MedicalConditionBuilder<P> symptom(Symptom symptom, int stages) {
        return symptom(new Symptom.ConfiguredSymptom(symptom, stages));
    }

    public MedicalConditionBuilder<P> symptom(Symptom symptom) {
        return symptom(new Symptom.ConfiguredSymptom(symptom));
    }

    @Override
    protected MedicalCondition createEntry() {
        return new MedicalCondition(ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName()),
                color, maxProgression, progressionType, progressionRate, canBePermenant, recipeModifier, symptoms.toArray(Symptom.ConfiguredSymptom[]::new));
    }
}
