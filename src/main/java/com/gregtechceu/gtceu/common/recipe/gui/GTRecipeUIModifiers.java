package com.gregtechceu.gtceu.common.recipe.gui;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.List;

public class GTRecipeUIModifiers {

    public static final RecipeUIModifier TEMP_COIL_INFO = (recipe, widget) -> {
        if (recipe.data.contains("ebf_temp")) {
            int temp = recipe.data.getInt("ebf_temp");

            widget.textComponents.child(new TextWidget<>(
                    Text.lang("gtceu.recipe.temperature", FormattingUtil.formatTemperature(temp))));

            Flow coilRow = Flow.row().coverChildrenHeight();

            ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);

            if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
                coilRow.child(new TextWidget<>(Text.lang("gtceu.recipe.coil.tier",
                        Component.translatable(requiredCoil.getMaterial().getUnlocalizedName())
                                .getString())));
            }

            List<ItemStack> items = GTCEuAPI.HEATING_COILS.entrySet().stream()
                    .filter(coil -> coil.getKey().getCoilTemperature() >= temp)
                    .map(coil -> new ItemStack(coil.getValue().get())).toList();

            coilRow.child(RecipeViewerSlotWidget.create()
                    .recipeSlotRole(RecipeSlotRole.RENDER_ONLY)
                    .value(ItemStackList.of(items))
                    .background(IDrawable.EMPTY)
                    .right(20));

            widget.textComponents.child(coilRow);
        }
    };

    public static final RecipeUIModifier RESEARCH_INFO = (recipe, widget) -> {
        for (var condition : recipe.conditions) {
            if (condition instanceof ResearchCondition researchCondition) {
                var row = Flow.row();
                for (var researchItem : researchCondition.getData()) {
                    row.child(RecipeViewerSlotWidget.create()
                            .marginLeft(2)
                            .recipeSlotRole(RecipeSlotRole.CATALYST)
                            .value(researchItem.dataItem()));
                }
                widget.child(row.name("research_info"));
            }
        }
    };
}
