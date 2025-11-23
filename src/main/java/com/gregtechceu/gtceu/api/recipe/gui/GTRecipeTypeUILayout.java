package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

public class GTRecipeTypeUILayout {

    @Setter
    private GTRecipeType recipeType;
    @Getter
    private UITexture progressBar;
    private int progressSize;
    @Getter
    private Map<IO, Map<RecipeCapability<?>, Int2ObjectOpenHashMap<IDrawable>>> overlays = new EnumMap<>(IO.class);

    private ParentWidget<?> parentWidget = null;

    public GTRecipeTypeUILayout() {}

    public ParentWidget<?> getMainWidget() {
        if (recipeType != null) {
            parentWidget = new ParentWidget<>();

            var inputItemGrid = createGrid(recipeType.getMaxInputs(ItemRecipeCapability.CAP), 3, false, 'i');
            var inputFluidGrid = createGrid(recipeType.getMaxInputs(FluidRecipeCapability.CAP), 3, false, 'f');
            var outputItemGrid = createGrid(recipeType.getMaxOutputs(ItemRecipeCapability.CAP), 3, true, 'i');
            var outputFluidGrid = createGrid(recipeType.getMaxOutputs(FluidRecipeCapability.CAP), 3, true, 'f');

            parentWidget.size(170, 64)
                    .padding(4)
                    .coverChildren()
                    .background(GTGuiTextures.BACKGROUND);
            Row mainRow = new Row();
            int width = 0;

            var IOs = new IO[] { IO.IN, IO.OUT };

            for (var io : IOs) {
                // var io = ioMap.getKey();
                var caps = (io == IO.IN ? recipeType.maxInputs : recipeType.maxOutputs);
                int slotHeight = 0;

                Column ioColumn = new Column();
                ioColumn.coverChildrenWidth();
                int ioWidth = 0;

                for (var recipeCap : caps.keySet()) {
                    var maxSlots = (io == IO.IN ? recipeType.getMaxInputs(recipeCap) :
                            recipeType.getMaxOutputs(recipeCap));
                    if (maxSlots == 0 || recipeCap == EURecipeCapability.CAP) continue;
                    char key = (recipeCap == ItemRecipeCapability.CAP ? 'i' : 'f');
                    var grid = createGrid(maxSlots, 3, io == IO.OUT, key);

                    slotHeight += 18 * grid.length;

                    IDrawable defaultSlotBackground = (recipeCap == ItemRecipeCapability.CAP ?
                            GTGuiTextures.SLOT : GTGuiTextures.FLUID_SLOT);

                    SlotGroupWidget.Builder slotWidget = SlotGroupWidget.builder()
                            .matrix(grid);

                    slotWidget.key(key, i -> {
                        var widget = new IDrawable.DrawableWidget(defaultSlotBackground);
                        if (overlays.containsKey(io) && overlays.get(io).containsKey(recipeCap)) {
                            widget.overlay(overlays.get(io).get(recipeCap).get(i));
                        }
                        return widget;
                    });

                    ioColumn.child(slotWidget.build().name(recipeCap.name + "_" + io.name()));

                    // calculate full width of each column
                    ioWidth = Math.max(ioWidth, Math.min(maxSlots, grid[0].length()) * 18);
                }
                width += ioWidth;
                mainRow.child(ioColumn.align(io == IO.IN ? Alignment.CenterLeft : Alignment.CenterRight));
            }

            width += (90 - (width / 2));

            mainRow.width(width);
            parentWidget.child(mainRow)
                    .child(new ProgressWidget()
                            .alignX(Alignment.CENTER)
                            .name("progressBar")
                            .texture(progressBar, progressSize))
                    .excludeAreaInXei();
        }
        return parentWidget;
    }

    // TODO move this when steam pr
    private static String[] createGrid(int amount, int rowSize, boolean output, char key) {
        int rows = (int) Math.ceil((float) amount / rowSize);
        String[] grid = new String[rows];
        for (int i = 0; i < rows; i++) {
            StringBuilder r = new StringBuilder();
            if (output) {
                for (int j = 0; j < rowSize; j++) {
                    if ((i * rowSize + j) > (amount - 1)) {
                        r.insert(0, " ");
                    } else {
                        r.insert(0, key);
                    }
                }
            } else {
                for (int j = 0; j < rowSize; j++) {
                    if ((i * rowSize + j) > (amount - 1)) {
                        r.append(" ");
                    } else {
                        r.append(key);
                    }
                }
            }
            grid[i] = r.toString();
        }

        return grid;
    }

    public static class Builder {

        private UITexture progressBar;
        private int progressSize;
        private Map<IO, Map<RecipeCapability<?>, Int2ObjectOpenHashMap<IDrawable>>> overlays = new EnumMap<>(IO.class);

        public Builder setSlotOverlay(IO ioMode, int slotIndex, RecipeCapability<?> cap, IDrawable overlay) {
            overlays.computeIfAbsent(ioMode, it -> new Object2ReferenceOpenHashMap<>())
                    .computeIfAbsent(cap, it -> new Int2ObjectOpenHashMap<>())
                    .put(slotIndex, overlay);
            return this;
        }

        public Builder setItemSlotOverlay(IO ioMode, int slotIndex, IDrawable overlay) {
            return setSlotOverlay(ioMode, slotIndex, ItemRecipeCapability.CAP, overlay);
        }

        public Builder setFluidSlotOverlay(IO ioMode, int slotIndex, IDrawable overlay) {
            return setSlotOverlay(ioMode, slotIndex, FluidRecipeCapability.CAP, overlay);
        }

        public Builder setItemSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd, IDrawable overlay) {
            for (int i = slotIndexStart; i <= slotIndexEnd; i++) {
                setSlotOverlay(ioMode, i, ItemRecipeCapability.CAP, overlay);
            }
            return this;
        }

        public Builder setFluidSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd, IDrawable overlay) {
            for (int i = slotIndexStart; i <= slotIndexEnd; i++) {
                setSlotOverlay(ioMode, i, FluidRecipeCapability.CAP, overlay);
            }
            return this;
        }

        public Builder setProgressBar(UITexture progressBar, int progressSize) {
            this.progressBar = progressBar;
            this.progressSize = progressSize;
            return this;
        }

        public GTRecipeTypeUILayout build() {
            GTRecipeTypeUILayout layout = new GTRecipeTypeUILayout();
            layout.progressBar = progressBar;
            layout.overlays = overlays;
            return layout;
        }
    }
}
