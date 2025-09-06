package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.network.chat.Component;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import earth.terrarium.heracles.common.handlers.progress.QuestProgressHandler;
import earth.terrarium.heracles.common.handlers.progress.QuestsProgress;
import earth.terrarium.heracles.common.handlers.quests.QuestHandler;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class HeraclesQuestCondition extends RecipeCondition {

    public static final Codec<HeraclesQuestCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.STRING.fieldOf("questId").forGetter(val -> val.questId))
                    .apply(instance, HeraclesQuestCondition::new));

    public final static HeraclesQuestCondition INSTANCE = new HeraclesQuestCondition();

    private String questId;

    public HeraclesQuestCondition(String questId) {
        this.questId = questId;
    };

    public HeraclesQuestCondition(boolean isReverse, String questId) {
        super(isReverse);
        this.questId = questId;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.HERACLES_QUEST;
    }

    @Override
    public Component getTooltips() {
        String questTitle = QuestHandler.get(questId).display().title().toString();

        if (isReverse) {
            return Component.translatable("recipe.condition.quest.not_completed.tooltip", questTitle);
        } else {
            return Component.translatable("recipe.condition.quest.completed.tooltip", questTitle);
        }
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        MachineOwner owner = recipeLogic.machine.self().getOwner();
        if (owner == null) return false;
        for (var player : owner.getMembers()) {
            QuestsProgress questsProgress = QuestProgressHandler
                    .getProgress(ServerLifecycleHooks.getCurrentServer(), player);
            var progress = questsProgress.getProgress(questId);
            if (progress != null && (progress.isComplete() || QuestHandler.get(questId).tasks().isEmpty())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new HeraclesQuestCondition();
    }
}
