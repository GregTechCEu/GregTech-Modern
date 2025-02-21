package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbquests.FTBQuestsAPIImpl;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.FTBTeamsAPIImpl;
import dev.ftb.mods.ftbteams.api.Team;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;
import java.util.Objects;

@NoArgsConstructor
public class FTBQuestCondition extends RecipeCondition {

    public static final Codec<FTBQuestCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.STRING.fieldOf("questId").forGetter(val -> val.questId))
                    .apply(instance, FTBQuestCondition::new));

    public final static FTBQuestCondition INSTANCE = new FTBQuestCondition();

    private String questId;


    public FTBQuestCondition(String questId, boolean isReverse) {
        super(isReverse);
        this.questId = questId;
    }

    public FTBQuestCondition(boolean isReverse, String questId) {
        super(isReverse);
        this.questId = questId;
    }

    public FTBQuestCondition(String questId) {
        this.questId = questId;
    };

    public FTBQuestCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.FTB_QUEST;
    }

    @Override
    public Component getTooltips() {
        if (isReverse) {
            return Component.translatable("recipe.condition.ftb_quest.not_completed.tooltip");
        } else {
            return Component.translatable("recipe.condition.ftb_quest.completed.tooltip");
        }
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        IMachineOwner owner = recipeLogic.machine.self().getHolder().getOwner();
        Team team = FTBTeamsAPIImpl.INSTANCE.getManager().getTeamForPlayerID(owner.getUUID()).orElse(null);
        BaseQuestFile questFile = FTBQuestsAPIImpl.INSTANCE.getQuestFile(false);
        long parsedQuestId = QuestObjectBase.parseCodeString(questId);
        QuestObject quest = questFile.get(parsedQuestId);

        GTCEu.LOGGER.info("Quest ID: {}", parsedQuestId);

        return questFile.getOrCreateTeamData(team).isCompleted(quest);
    }

    @Override
    public RecipeCondition createTemplate() {
        return new FTBQuestCondition();
    }
}
