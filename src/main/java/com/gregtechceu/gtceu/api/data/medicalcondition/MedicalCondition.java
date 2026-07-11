package com.gregtechceu.gtceu.api.data.medicalcondition;

import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.misc.AirScrubberRecipes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@Accessors(chain = true)
public class MedicalCondition {

    public static final String AFFECTED_SUFFIX = ".affected";

    /**
     * The ID of this medical condition in the registry.<br>
     * The tooltip text is derived from this in the form of {@code "medical_condition.<namespace>.<path>"}
     * <p>
     * If the name of this medical condition is queried in the context of affecting a player, and a language key
     * {@code "medical_condition.<namespace>.<path>.affected"} exists, it'll be used instead of the generic one.<br>
     * For example, the {@code gtceu:carcinogenic} medical condition's tooltip name is "Carcinogenic".
     * When a player with cancer checks their status with {@code /medical_condition query},
     * the command will display "Player &lt;player&gt; has cancer" instead of "... has Carcinogenic"
     * </p>
     */
    public final ResourceLocation id;
    public final int color;
    public final float maxProgression; // amount of seconds until maximum progression is reached
    public final Set<Symptom.ConfiguredSymptom> symptoms = new HashSet<>();
    public final IdleProgressionType idleProgressionType;
    public final float idleProgressionRate;
    public final boolean canBePermanent;
    /**
     * This should mirror the associated {@linkplain AirScrubberRecipes air scrubber recipe's} outputs for this
     * condition.
     */
    @Getter
    @Setter
    @NotNull
    public Consumer<GTRecipeBuilder> recipeModifier = builder -> {};

    public MedicalCondition(ResourceLocation id, int color,
                            int maxProgression, IdleProgressionType progressionType, float progressionRate,
                            boolean canBePermanent,
                            Symptom.ConfiguredSymptom... symptoms) {
        this.id = id;
        this.color = color;
        this.maxProgression = maxProgression;

        for (Symptom.ConfiguredSymptom symptom : symptoms) {
            symptom.addedToCondition(this, this.symptoms.size());
            this.symptoms.add(symptom);
        }

        this.idleProgressionType = progressionType;
        this.idleProgressionRate = progressionRate;
        this.canBePermanent = canBePermanent;
    }

    public ResourceKey<DamageType> getDamageType() {
        return ResourceKey.create(Registries.DAMAGE_TYPE, id.withPrefix("medical_condition/"));
    }

    public String getTranslationKey() {
        return this.id.toLanguageKey("medical_condition");
    }

    public Component getTranslatableName() {
        return Component.translatable(this.getTranslationKey()).withStyle(style -> style.withColor(this.color));
    }

    public Component getAffectedName() {
        String key = this.getTranslationKey();
        String affectedKey = key + AFFECTED_SUFFIX;
        if (Language.getInstance().has(affectedKey)) {
            key = affectedKey;
        }
        return Component.translatable(key).withStyle(style -> style.withColor(this.color));
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(", ", "[", "]");
        stringJoiner.add("color=#" + FormattingUtil.HEX_FORMAT.toHexDigits(this.color));
        stringJoiner.add("maxProgression=" + this.maxProgression);
        stringJoiner.add("symptoms=" + this.symptoms);
        stringJoiner.add("damageType=" + this.getDamageType());
        stringJoiner.add("idleProgressionType=" + this.idleProgressionType.name().toLowerCase(Locale.ROOT));
        stringJoiner.add("idleProgressionRate=" + this.idleProgressionRate);

        return this.id.toString() + stringJoiner;
    }

    public enum IdleProgressionType {
        UNTREATED_PROGRESSION,
        HEAL,
        NONE
    }
}
