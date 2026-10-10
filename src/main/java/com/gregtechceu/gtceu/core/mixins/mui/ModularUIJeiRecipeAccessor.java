package com.gregtechceu.gtceu.core.mixins.mui;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(value = ModularUIJeiCategory.class, remap = false)
public interface ModularUIJeiRecipeAccessor {

    @Accessor
    Function<?, IWidget> getRecipeUI();
}
