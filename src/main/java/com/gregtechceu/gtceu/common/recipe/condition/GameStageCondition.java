package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class GameStageCondition extends RecipeCondition<GameStageCondition> {

    // spotless:off
    public static final Codec<GameStageCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            Codec.STRING.fieldOf("stageName").forGetter(GameStageCondition::getStageName)
    ).apply(instance, GameStageCondition::new));
    // spotless:on

    @Getter(AccessLevel.PRIVATE)
    private String stageName;

    public GameStageCondition(String stageName) {
        this(false, stageName);
    }

    public GameStageCondition(boolean isReverse, String stageName) {
        super(isReverse);
        this.stageName = stageName;
    }

    @Override
    public RecipeConditionType<GameStageCondition> getType() {
        return GTRecipeConditions.GAMESTAGE;
    }

    @Override
    public Component getTooltips() {
        if (isReverse) return Component.translatable("recipe.condition.gamestage.locked_stage", stageName);
        return Component.translatable("recipe.condition.gamestage.unlocked_stage", stageName);
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        MachineOwner owner = recipeLogic.getMachine().getOwner();
        if (owner == null) return false;
        for (var player : owner.getMembers()) {
            var playerData = GameStageSaveHandler.getPlayerData(player);
            if (playerData != null && playerData.hasStage(stageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GameStageCondition createTemplate() {
        return new GameStageCondition();
    }
}
