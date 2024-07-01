package com.gregtechceu.gtceu.common.recipe;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.data.medicalcondition.GTMedicalConditions;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalHazardCondition extends RecipeCondition {

    public static final MapCodec<EnvironmentalHazardCondition> CODEC = RecordCodecBuilder
            .mapCodec(instance -> RecipeCondition.isReverse(instance)
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
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled) return true;
        if (!(recipeLogic.getMachine().getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }
        EnvironmentalHazardSavedData savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
        var zone = savedData.getZoneByContainedPos(recipeLogic.getMachine().getPos());
        return zone != null && zone.strength() > 0;
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject value = super.serialize();
        value.addProperty("condition", condition.name);
        return value;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        this.condition = MedicalCondition.CONDITIONS.get(GsonHelper.getAsString(config, "condition"));
        return this;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new EnvironmentalHazardCondition();
    }
}
