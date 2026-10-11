package com.gregtechceu.gtceu.core.mixins.jei;

import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipesGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RecipesGui.class, remap = false)
public interface RecipesGuiAccessor {

    @Accessor("logic")
    IRecipeGuiLogic gtceu$getLogic();
}
