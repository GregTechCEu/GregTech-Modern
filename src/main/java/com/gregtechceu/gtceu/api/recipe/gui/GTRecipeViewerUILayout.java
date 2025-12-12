package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.util.ArrayList;
import java.util.Map;

public class GTRecipeViewerUILayout {

    public final GTRecipeTypeUILayout layout;

    private ParentWidget<? extends ParentWidget<?>> overrideWidget;

    public GTRecipeViewerUILayout(GTRecipeTypeUILayout layout) {
        this.layout = layout;
    }

    public GTRecipeViewerUILayout overrideWidget(ParentWidget<? extends ParentWidget<?>> overrideWidget) {
        this.overrideWidget = overrideWidget;
        return this;
    }

    public ParentWidget<?> getRecipeWidget() {
        if (layout.getRecipeType() != null) {
            var parentWidget = new ParentWidget<>();
            parentWidget.coverChildren();
            var slotsRow = new Row();
            slotsRow.coverChildrenHeight();

            int rowWidthPx = 0;

            Map<IO, ParentWidget<?>> colWidgetGroups = new Object2ReferenceOpenHashMap<>();

            int slotLeftShiftPx = 0;
            for (var io : layout.getIOs()) {
                boolean in = io == IO.IN;

                var caps = (in ? layout.getRecipeType().maxInputs : layout.getRecipeType().maxOutputs);
                int slotGroupHeightPx = 0;

                Column ioColumn = new Column();
                // ioColumn.coverChildrenWidth();
                int slotGroupWidthPx = 0;

                var widgetGroups = new ArrayList<ParentWidget<?>>();

                for (var recipeCap : caps.keySet()) {
                    int maxRecipeTypeSlots = caps.get(recipeCap);

                    var grid = layout.createGrid(io, recipeCap, 's', GTValues.MAX, Integer.MAX_VALUE);

                    slotGroupHeightPx += 18 * grid.length;

                    IDrawable defaultSlotBackground = (recipeCap == ItemRecipeCapability.CAP ?
                            GTGuiTextures.SLOT : GTGuiTextures.FLUID_SLOT);

                    SlotGroupWidget.Builder slotWidgetBuilder = SlotGroupWidget.builder()
                            .matrix(grid);
                    var overlays = layout.getOverlays();
                    slotWidgetBuilder.key('s', i -> {
                        var widget = new IDrawable.DrawableWidget(defaultSlotBackground);
                        if (overlays.containsKey(io) && overlays.get(io).containsKey(recipeCap)) {
                            widget.overlay(overlays.get(io).get(recipeCap).get(i));
                        }
                        return widget;
                    });

                    // calculate full width of each column
                    slotGroupWidthPx = Math.max(slotGroupWidthPx, Math.min(maxRecipeTypeSlots, grid[0].length()) * 18);

                    widgetGroups.add(slotWidgetBuilder.build()
                            .name(recipeCap.name + "_" + io.name())
                            .alignX(io == IO.IN ? Alignment.TopLeft : Alignment.TopRight));
                }

                ioColumn.size(slotGroupWidthPx, slotGroupHeightPx);
                for (var g : widgetGroups) {
                    ioColumn.child(g);
                }
                slotLeftShiftPx += (slotGroupWidthPx / 2) * ((io == io.IN) ? -1 : 1);

                rowWidthPx += slotGroupWidthPx;
                colWidgetGroups.put(io, ioColumn);
            }
            // 2 px padding plus each half of the progress bar (1)
            slotsRow.childPadding((layout.getProgressSize() / 2) + 2);
            for (var ioColumn : colWidgetGroups.entrySet()) {
                var col = ioColumn.getValue();
                var io = ioColumn.getKey();
                slotsRow.child(col.align(io == IO.IN ? Alignment.CenterLeft : Alignment.CenterRight));
            }

            // same padding as (1) + half a slot on each side
            rowWidthPx += layout.getProgressSize() + 4 + 18;
            slotsRow.width(rowWidthPx);

            parentWidget.child(slotsRow.left(slotLeftShiftPx));

            parentWidget.child(new ProgressWidget()
                    .center()
                    .progress(() -> 0.5f)
                    .name("progressBar")
                    .texture(layout.getProgressBar(), layout.getProgressSize())
                    .size(layout.getProgressSize())
                    .direction(layout.getProgressDirection()));
            return parentWidget;
        }
        return new ParentWidget<>();
    }
}
