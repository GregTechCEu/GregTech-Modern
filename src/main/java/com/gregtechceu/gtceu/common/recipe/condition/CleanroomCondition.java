package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.CleanroomReceiverTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@NoArgsConstructor
public class CleanroomCondition extends RecipeCondition<CleanroomCondition> {

    // spotless:off
    public static final Codec<CleanroomCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            CleanroomType.CODEC.fieldOf("cleanroom").forGetter(val -> val.cleanroom)
    ).apply(instance, CleanroomCondition::new));
    // spotless:on

    @Getter
    private CleanroomType cleanroom = CleanroomType.CLEANROOM;

    public CleanroomCondition(boolean isReverse, CleanroomType cleanroom) {
        super(isReverse);
        this.cleanroom = cleanroom;
    }

    @Override
    public RecipeConditionType<CleanroomCondition> getType() {
        return GTRecipeConditions.CLEANROOM;
    }

    @Override
    public Component getTooltips() {
        return cleanroom == null ? null :
                Component.translatable("gtceu.recipe.cleanroom", Component.translatable(cleanroom.getTranslationKey()));
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        MetaMachine machine = recipeLogic.getMachine();

        if (!ConfigHolder.INSTANCE.machines.enableCleanroom) return true;
        if (ConfigHolder.INSTANCE.machines.cleanMultiblocks && machine instanceof MultiblockControllerMachine)
            return true;

        CleanroomReceiverTrait receiverTrait = machine.getTraitHolder().getTrait(CleanroomReceiverTrait.TYPE);

        if (receiverTrait != null && this.cleanroom != null) return receiverTrait.hasActiveCleanroom(cleanroom);
        return true;
    }

    @Override
    public CleanroomCondition createTemplate() {
        return new CleanroomCondition();
    }
}
