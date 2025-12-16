package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalHazardCondition extends RecipeCondition {

    public static final Codec<EnvironmentalHazardCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(
                            MedicalCondition.CODEC.fieldOf("condition").forGetter(val -> val.condition))
                    .apply(instance, EnvironmentalHazardCondition::new));

    @Getter
    private MedicalCondition condition = GTMedicalConditions.CARBON_MONOXIDE_POISONING;

    public EnvironmentalHazardCondition(boolean isReverse, MedicalCondition condition) {
        super(isReverse);
        this.condition = condition;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.ENVIRONMENTAL_HAZARD;
    }

    @Override
    public Component getTooltips() {
        return isReverse ?
                Component.translatable("gtceu.recipe.environmental_hazard.reverse",
                        Component.translatable("gtceu.medical_condition." + condition.name)) :
                Component.translatable("gtceu.recipe.environmental_hazard",
                        Component.translatable("gtceu.medical_condition." + condition.name));
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) return true;
        if (!(recipeLogic.getMachine().getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        EnvironmentalHazardSavedData savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
        var zone = savedData.getZoneByContainedPos(recipeLogic.getMachine().getPos());
        return zone != null && zone.strength() > 0;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new EnvironmentalHazardCondition();
    }
}
