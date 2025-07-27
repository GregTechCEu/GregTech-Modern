package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.emi.ModularWrapperWidget;
import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(value = ModularWrapperWidget.class, remap = false)
public class ModularWrapperWidgetMixin {

    @WrapOperation(method = "getTooltip(II)Ljava/util/List;",
                   constant = { @Constant(classValue = IRecipeIngredientSlot.class) })
    public boolean gtceu$wrapInstanceOf(Object object, Operation<Boolean> original) {
        return false;
    }
}
