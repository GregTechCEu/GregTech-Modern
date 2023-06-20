package com.gregtechceu.gtceu.common.recipe;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@NoArgsConstructor
public class CleanroomCondition extends RecipeCondition {
    public final static CleanroomCondition INSTANCE = new CleanroomCondition();

    @Nullable
    @Getter
    private CleanroomType cleanroom = null;

    @Override
    public String getType() {
        return "cleanroom";
    }

    @Override
    public Component getTooltips() {
        return cleanroom == null ? null : Component.translatable("gtceu.recipe.cleanroom", Component.translatable(cleanroom.getTranslationKey()));
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        MetaMachine machine = recipeLogic.getMachine();
        if (machine instanceof ICleanroomReceiver receiver) {
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
