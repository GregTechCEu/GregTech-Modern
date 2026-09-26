package com.gregtechceu.gtceu.core.mixins.mui;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(ModularUIEmiRecipe.class)
public interface ModularUIEmiRecipeAccessor {

    @Accessor
    Supplier<IWidget> getRecipeUI();
}
