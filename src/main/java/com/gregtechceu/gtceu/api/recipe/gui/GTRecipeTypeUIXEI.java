package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

public class GTRecipeTypeUIXEI {

    public final GTRecipeTypeUILayout layout;

    private ParentWidget<? extends ParentWidget<?>> overrideWidget;

    public GTRecipeTypeUIXEI(GTRecipeTypeUILayout layout) {
        this.layout = layout;
    }

    public GTRecipeTypeUIXEI overrideWidget(ParentWidget<? extends ParentWidget<?>> overrideWidget) {
        this.overrideWidget = overrideWidget;
        return this;
    }

    public ParentWidget<? extends ParentWidget<?>> getWidget() {
        if(overrideWidget != null) {
            return overrideWidget;
        }



        ParentWidget<?> parent = new ParentWidget<>();

        return parent;
    }






}
