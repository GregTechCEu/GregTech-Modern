package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
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
public class CleanroomCondition extends RecipeCondition {

    public static final Codec<CleanroomCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(CleanroomType.CODEC.fieldOf("cleanroom").forGetter(val -> val.cleanroom))
                    .apply(instance, CleanroomCondition::new));
    public final static CleanroomCondition INSTANCE = new CleanroomCondition();

    @Getter
    private CleanroomType cleanroom = CleanroomType.CLEANROOM;

    public CleanroomCondition(boolean isReverse, CleanroomType cleanroom) {
        super(isReverse);
        this.cleanroom = cleanroom;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTRecipeConditions.CLEANROOM;
    }

    @Override
    public Component getTooltips() {
        return cleanroom == null ? null :
                Component.translatable("gtceu.recipe.cleanroom", Component.translatable(cleanroom.getTranslationKey()));
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (!ConfigHolder.INSTANCE.machines.enableCleanroom) return true;
        MetaMachine machine = recipeLogic.getMachine();
        if (machine instanceof ICleanroomReceiver receiver && this.cleanroom != null) {
            if (ConfigHolder.INSTANCE.machines.cleanMultiblocks && machine instanceof IMultiController) return true;

            ICleanroomProvider provider = receiver.getCleanroom();
            if (provider == null) return false;

            return provider.isClean() && provider.getTypes().contains(this.cleanroom);
        }
        return true;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new CleanroomCondition();
    }
}
