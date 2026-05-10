package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MULTIBLOCK;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.register;

public class GCYMRecipeTypes {

    //////////////////////////////////////
    // ******* Multiblock *******//
    //////////////////////////////////////
    public final static GTRecipeType ALLOY_BLAST_RECIPES = register("alloy_blast_smelter", MULTIBLOCK)
            .setMaxIOSize(9, 0, 3, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder
                    .setItemSlotsOverlay(IO.IN, 0, 8, GTGuiTextures.FURNACE_OVERLAY_1)
                    .setFluidSlotsOverlay(IO.IN, 0, 2, GTGuiTextures.FURNACE_OVERLAY_2)
                    .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.FURNACE_OVERLAY_2)
                    .setProgressBar(GTGuiTextures.PROGRESS_BAR_ARROW, 20, ProgressWidget.Direction.RIGHT)
                    .addRecipeUIModifier((recipe, widget) -> {
                        if (recipe.data.contains("ebf_temp")) {
                            int temp = recipe.data.getInt("ebf_temp");

                            widget.textComponents.child(new TextWidget<>(
                                    Text.lang("gtceu.recipe.temperature", FormattingUtil.formatTemperature(temp))));

                            Flow coilRow = Flow.row();

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
                                    .right(4));

                            widget.textComponents.child(coilRow);
                        }
                    }))
            .setSound(GTSoundEntries.ARC);

    public static void init() {}
}
