package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.DoubleSupplier;

public class GTRecipeTypeUILayout {

    @Setter
    @Getter
    private GTRecipeType recipeType;
    @Getter
    private UITexture progressBar;
    @Getter
    private int progressSize;
    @Getter
    private ProgressWidget.Direction progressDirection;
    @Getter
    private Set<IO> IOs = new HashSet<>();
    @Getter
    private Map<IO, Map<RecipeCapability<?>, Int2ObjectOpenHashMap<IDrawable>>> overlays = new EnumMap<>(IO.class);
    private Map<IO, Map<RecipeCapability<?>, Int2IntArrayMap>> gridLength = new EnumMap<>(IO.class);
    private Map<IO, Map<RecipeCapability<?>, Int2IntArrayMap>> gridWidths = new EnumMap<>(IO.class);

    private ParentWidget<?> parentWidget = null;

    public GTRecipeTypeUILayout() {}

    public ParentWidget<?> getBackedSlotsRow(@NotNull PanelSyncManager syncManager,
                                             @Nullable NotifiableItemStackHandler inputItems,
                                             @Nullable NotifiableItemStackHandler outputItems,
                                             @Nullable NotifiableFluidTank inputFluids,
                                             @Nullable NotifiableFluidTank outputFluids,
                                             DoubleSupplier progressSupplier, int tier) {
        if (recipeType != null) {
            var backedSlotsPanel = new ParentWidget<>();
            backedSlotsPanel.coverChildren();
            var backedSlotsRow = new Row();
            backedSlotsRow.coverChildrenHeight();

            int rowWidthPx = 0;

            // List<IO> IOs = new ArrayList<>();
            if (inputItems != null || inputFluids != null) {
                IOs.add(IO.IN);
            }
            if (outputFluids != null || outputItems != null) {
                IOs.add(IO.OUT);
            }

            Map<IO, ParentWidget<?>> colWidgetGroups = new Object2ReferenceOpenHashMap<>();

            int slotLeftShiftPx = 0;
            for (var io : IOs) {
                boolean in = io == IO.IN;

                var caps = (in ? recipeType.maxInputs : recipeType.maxOutputs);
                int slotGroupHeightPx = 0;

                Column ioColumn = new Column();
                // ioColumn.coverChildrenWidth();
                int slotGroupWidthPx = 0;

                var widgetGroups = new ArrayList<ParentWidget<?>>();

                for (var recipeCap : caps.keySet()) {
                    int maxRecipeTypeSlots = caps.get(recipeCap);
                    int maxMachineSlots = 0;
                    if (maxRecipeTypeSlots == 0 || recipeCap == EURecipeCapability.CAP) continue;
                    if (recipeCap == ItemRecipeCapability.CAP) {
                        maxMachineSlots = in ? inputItems.getSlots() : outputItems.getSlots();
                    } else if (recipeCap == FluidRecipeCapability.CAP) {
                        maxMachineSlots = in ? inputFluids.getTanks() : outputFluids.getTanks();
                    }

                    var grid = createGrid(io, recipeCap, 's', tier, maxMachineSlots);

                    slotGroupHeightPx += 18 * grid.length;

                    IDrawable defaultSlotBackground = (recipeCap == ItemRecipeCapability.CAP ?
                            GTGuiTextures.SLOT : GTGuiTextures.FLUID_SLOT);

                    SlotGroupWidget.Builder slotWidgetBuilder = SlotGroupWidget.builder()
                            .matrix(grid);

                    if (recipeCap == ItemRecipeCapability.CAP) {
                        var handler = in ? inputItems : outputItems;
                        SlotGroup group = new SlotGroup("item_" + io.name(), grid[0].length());
                        if (handler != null) {
                            slotWidgetBuilder.key('s', i -> {
                                var overlay = IDrawable.EMPTY;

                                if (overlays.containsKey(io) && overlays.get(io).containsKey(recipeCap)) {
                                    overlay = overlays.get(io).get(recipeCap).get(i) != null ?
                                            overlays.get(io).get(recipeCap).get(i) : IDrawable.EMPTY;
                                }

                                return new ItemSlot().slot(new ModularSlot(handler, i)
                                        .slotGroup(group))
                                        .background(defaultSlotBackground, overlay);
                            });
                        }
                    } else if (recipeCap == FluidRecipeCapability.CAP) {
                        var handler = in ? inputFluids : outputFluids;
                        String syncHandlerName = "fluid_" + io.name();
                        for (int i = 0; i < maxMachineSlots; i++) {
                            syncManager.syncValue(syncHandlerName, i, SyncHandlers.fluidSlot(handler.getStorages()[i]));
                        }
                        if (handler != null) {
                            slotWidgetBuilder.key('s', i -> {
                                var overlay = IDrawable.EMPTY;

                                if (overlays.containsKey(io) && overlays.get(io).containsKey(recipeCap)) {
                                    overlay = overlays.get(io).get(recipeCap).get(i) != null ?
                                            overlays.get(io).get(recipeCap).get(i) : IDrawable.EMPTY;
                                }

                                return new FluidSlot()
                                        .syncHandler(syncHandlerName, i)
                                        .background(defaultSlotBackground, overlay);
                            });
                        }
                    }

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
            backedSlotsRow.childPadding((progressSize / 2) + 2);
            for (var ioColumn : colWidgetGroups.entrySet()) {
                var col = ioColumn.getValue();
                var io = ioColumn.getKey();
                backedSlotsRow.child(col.align(io == IO.IN ? Alignment.CenterLeft : Alignment.CenterRight));
            }

            // same padding as (1) + half a slot on each side
            rowWidthPx += progressSize + 4 + 18;
            backedSlotsRow.width(rowWidthPx);

            backedSlotsPanel.child(backedSlotsRow.left(slotLeftShiftPx));

            backedSlotsPanel.child(new ProgressWidget()
                    .center()
                    .progress(progressSupplier)
                    .name("progressBar")
                    .texture(progressBar, progressSize)
                    .size(progressSize)
                    .direction(progressDirection));
            return backedSlotsPanel;
        }
        return GTGuis.createPanel("empty");
    }

    public String[] createGrid(IO io, RecipeCapability<?> cap, char key, int tier, int maxMachineSlots) {
        int maxWidth = 3;
        if (gridWidths.containsKey(io) && gridWidths.get(io).containsKey(cap)) {
            int width = gridWidths.get(io).get(cap).get(tier);
            if (width != 0) maxWidth = width;
        }
        int maxSlots = (io == IO.IN ? recipeType.getMaxInputs(cap) : recipeType.getMaxOutputs(cap));
        if (gridLength.containsKey(io) && gridLength.get(io).containsKey(cap)) {
            int length = gridLength.get(io).get(cap).get(tier);
            if (length != 0) maxSlots = length;
        }
        maxSlots = Math.min(maxMachineSlots, maxSlots);
        maxWidth = Math.min(maxSlots, maxWidth);
        return GTMuiWidgets.createGrid(maxSlots, maxWidth, io.support(IO.OUT), key);
    }

    public static class Builder {

        private UITexture progressBar;
        private int progressSize;
        private ProgressWidget.Direction fillDirection;
        private Map<IO, Map<RecipeCapability<?>, Int2ObjectOpenHashMap<IDrawable>>> overlays = new EnumMap<>(IO.class);
        private Map<IO, Map<RecipeCapability<?>, Int2IntArrayMap>> gridLength = new EnumMap<>(IO.class);
        private Map<IO, Map<RecipeCapability<?>, Int2IntArrayMap>> gridWidths = new EnumMap<>(IO.class);

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
            return setProgressBar(progressBar, progressSize, ProgressWidget.Direction.RIGHT);
        }

        public Builder setProgressBar(UITexture progressBar, int progressSize, ProgressWidget.Direction fillDirection) {
            this.progressBar = progressBar;
            this.progressSize = progressSize;
            this.fillDirection = fillDirection;
            return this;
        }

        public Builder setIOSlotLength(IO ioMode, RecipeCapability<?> cap, int tier, int value) {
            gridLength.computeIfAbsent(ioMode, it -> new Object2ReferenceOpenHashMap<>())
                    .computeIfAbsent(cap, it -> new Int2IntArrayMap())
                    .put(tier, value);
            return this;
        }

        public Builder setIOSlotLengths(IO ioMode, RecipeCapability<?> cap, int startTier, int endTier, int value) {
            for (int i = startTier; i <= endTier; i++) {
                gridLength.computeIfAbsent(ioMode, it -> new Object2ReferenceOpenHashMap<>())
                        .computeIfAbsent(cap, it -> new Int2IntArrayMap())
                        .put(i, value);
            }
            return this;
        }

        public Builder setIOSlotWidth(IO ioMode, RecipeCapability<?> cap, int tier, int value) {
            gridWidths.computeIfAbsent(ioMode, it -> new Object2ReferenceOpenHashMap<>())
                    .computeIfAbsent(cap, it -> new Int2IntArrayMap())
                    .put(tier, value);
            return this;
        }

        public Builder setIOSlotWidths(IO ioMode, RecipeCapability<?> cap, int startTier, int endTier, int value) {
            for (int i = startTier; i <= endTier; i++) {
                gridWidths.computeIfAbsent(ioMode, it -> new Object2ReferenceOpenHashMap<>())
                        .computeIfAbsent(cap, it -> new Int2IntArrayMap())
                        .put(i, value);
            }
            return this;
        }

        public GTRecipeTypeUILayout build() {
            GTRecipeTypeUILayout layout = new GTRecipeTypeUILayout();
            layout.progressBar = progressBar;
            layout.progressSize = progressSize;
            layout.progressDirection = fillDirection;
            layout.overlays = overlays;
            layout.gridLength = gridLength;
            layout.gridWidths = gridWidths;
            return layout;
        }
    }
}
