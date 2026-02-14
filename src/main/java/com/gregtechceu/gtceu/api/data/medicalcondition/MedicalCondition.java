package com.gregtechceu.gtceu.api.data.medicalcondition;

import com.gregtechceu.gtceu.api.data.damagesource.DamageTypeData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.misc.AirScrubberRecipes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@Accessors(chain = true)
public class MedicalCondition {

    public static final Codec<MedicalCondition> CODEC = GTRegistries.MEDICAL_CONDITIONS.codec();
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
    private final DamageTypeData damageTypeData;
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
                            boolean canBePermanent, Symptom.ConfiguredSymptom... symptoms) {
        this.id = id;
        this.color = color;
        this.maxProgression = maxProgression;
        this.damageTypeData = new DamageTypeData.Builder()
                .simpleId(id.withPrefix("medical_condition/"))
                .scaling(DamageScaling.NEVER)
                // all medical conditions' damage types MUST have the bypasses_invulnerability and bypasses_cooldown
                // tags so the death symptom works properly
                .tag(DamageTypeTags.BYPASSES_ARMOR, DamageTypeTags.BYPASSES_RESISTANCE,
                        DamageTypeTags.BYPASSES_INVULNERABILITY, DamageTypeTags.BYPASSES_COOLDOWN)
                .build();

        for (Symptom.ConfiguredSymptom symptom : symptoms) {
            symptom.addedToCondition(this, this.symptoms.size());
            this.symptoms.add(symptom);
        }
        this.idleProgressionType = progressionType;
        this.idleProgressionRate = progressionRate;
        this.canBePermanent = canBePermanent;
    }

    public DamageSource getDamageSource(MedicalConditionTracker tracker) {
        return this.damageTypeData.source(tracker.getPlayer().level());
    }

    public DamageSource getDamageSource(Level level) {
        return this.damageTypeData.source(level);
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
        stringJoiner.add("damageType=" + this.damageTypeData.id);
        stringJoiner.add("idleProgressionType=" + this.idleProgressionType.name().toLowerCase(Locale.ROOT));
        stringJoiner.add("idleProgressionRate=" + this.idleProgressionRate);

        return this.id.toString() + stringJoiner.toString();
    }

    public enum IdleProgressionType {
        UNTREATED_PROGRESSION,
        HEAL,
        NONE
    }
}
