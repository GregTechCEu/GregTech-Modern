package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbteams.FTBTeamsAPIImpl;
import dev.ftb.mods.ftbteams.api.Team;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NoArgsConstructor
public class FTBQuestCondition extends RecipeCondition {

    public static final Codec<FTBQuestCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.STRING.fieldOf("questId").forGetter(val -> val.questId))
                    .apply(instance, FTBQuestCondition::new));

    public final static FTBQuestCondition INSTANCE = new FTBQuestCondition();

    private String questId;

    public FTBQuestCondition(String questId) {
        this.questId = questId;
    };

    public FTBQuestCondition(boolean isReverse, String questId) {
        super(isReverse);
        this.questId = questId;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.FTB_QUEST;
    }

    @Override
    public Component getTooltips() {
        BaseQuestFile questFile = FTBQuestsAPI.api().getQuestFile(false);
        long parsedQuestId = QuestObjectBase.parseCodeString(questId);
        Component questTitle = Objects.requireNonNull(questFile.get(parsedQuestId)).getTitle();

        if (isReverse) {
            return Component.translatable("recipe.condition.quest.not_completed.tooltip", questTitle);
        } else {
            return Component.translatable("recipe.condition.quest.completed.tooltip", questTitle);
        }
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        IMachineOwner owner = recipeLogic.machine.self().getHolder().getOwner();
        Team team = FTBTeamsAPIImpl.INSTANCE.getManager().getTeamForPlayerID(owner.getUUID()).orElse(null);
        BaseQuestFile questFile = FTBQuestsAPI.api().getQuestFile(false);
        long parsedQuestId = QuestObjectBase.parseCodeString(questId);
        QuestObject quest = questFile.get(parsedQuestId);

        return questFile.getOrCreateTeamData(team).isCompleted(quest);
    }

    @Override
    public RecipeCondition createTemplate() {
        return new FTBQuestCondition();
    }

    @Override
    public @NotNull JsonObject serialize() {
        var obj = super.serialize();
        obj.addProperty("questId", questId);
        return obj;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        questId = GsonHelper.getAsString(config, "questId");
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        questId = buf.readUtf();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeUtf(questId);
    }
}
