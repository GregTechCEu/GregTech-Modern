package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.value.IDoubleValue;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.Function;

/**
 * Holds UI information for a recipe type UI.
 */
public class GTRecipeTypeUILayout {

    @Getter
    private final GTRecipeType recipeType;
    @Getter
    private ProgressBarTextureSet progressBar;

    @Getter
    private final ProgressWidgetSupplier progressWidgetSupplier;
    private final Map<RecipeCapability<?>, CapabilityUIInfo> capabilityInfo;

    @Getter
    private final List<RecipeUIModifier> recipeUIModifiers;
    @Getter
    private final @Nullable Function<GTRecipe, Flow> customUIBuilder;

    public GTRecipeTypeUILayout(GTRecipeType recipeType, Map<RecipeCapability<?>, CapabilityUIInfo> capabilityInfo,
                                List<RecipeUIModifier> recipeUIModifiers,
                                ProgressWidgetSupplier progressWidgetSupplier,
                                @Nullable Function<GTRecipe, Flow> customUIBuilder) {
        this.recipeType = recipeType;
        this.capabilityInfo = capabilityInfo;
        this.recipeUIModifiers = recipeUIModifiers;
        this.progressWidgetSupplier = progressWidgetSupplier;
        this.customUIBuilder = customUIBuilder;
    }

    public CapabilityUIInfo capabilityInfo(RecipeCapability<?> cap) {
        var info = capabilityInfo.computeIfAbsent(cap, CapabilityUIInfo::new);
        if (info.layout == null) info.layout = this;
        return info;
    }

    public static class CapabilityUIInfo {

        private final Map<IO, Int2ObjectOpenHashMap<IDrawable>> overlays = new Object2ObjectOpenHashMap<>();

        private final Map<IO, MachineCapabilityGridBuilder> machineLayoutGridBuilders = new EnumMap<>(IO.class);

        public @Nullable MachineCapabilityLayoutBuilder machineLayoutBuilder;

        public @Nullable CapabilityContentBuilder capabilityWidgetBuilder;

        private final Map<IO, RecipeViewerCapabilityGridBuilder> recipeViewerLayoutGridBuilders = new EnumMap<>(
                IO.class);

        public @Nullable RecipeViewerCapabilityLayoutBuilder recipeViewerLayoutBuilder;

        private @UnknownNullability GTRecipeTypeUILayout layout;
        private final RecipeCapability<?> cap;

        private CapabilityUIInfo(RecipeCapability<?> cap) {
            this.cap = cap;
        }

        public IDrawable getOverlay(IO io, int index) {
            return overlays.computeIfAbsent(io, $ -> new Int2ObjectOpenHashMap<>()).getOrDefault(index,
                    IDrawable.EMPTY);
        }

        public String[] getMachineGrid(IO io, MetaMachine machine) {
            if (machineLayoutGridBuilders.containsKey(io))
                return machineLayoutGridBuilders.get(io).buildGrid(machine, layout);
            var slots = layout.recipeType.getMaxSlots(cap, io);
            return GTMuiWidgets.createGrid(slots, Math.min(3, slots), io.support(IO.OUT), 's');
        }

        public String[] getRecipeViewerGrid(IO io) {
            if (recipeViewerLayoutGridBuilders.containsKey(io))
                return recipeViewerLayoutGridBuilders.get(io).buildGrid(layout);
            return GTMuiWidgets.createGrid(layout.recipeType.getMaxSlots(cap, io),
                    Math.min(3, layout.recipeType.getMaxSlots(cap, io)), io.support(IO.OUT), 's');
        }
    }

    @FunctionalInterface
    public interface MachineCapabilityGridBuilder {

        String[] buildGrid(MetaMachine machine, GTRecipeTypeUILayout layout);
    }

    @FunctionalInterface
    public interface RecipeViewerCapabilityGridBuilder {

        String[] buildGrid(GTRecipeTypeUILayout layout);
    }

    @FunctionalInterface
    public interface ProgressWidgetSupplier {

        Widget<?> get(GTRecipeTypeUILayout layout, IDoubleValue<Double> value, @Nullable MetaMachine machine);
    }

    public static class Builder {

        private ProgressBarTextureSet progressBar = GTGuiTextures.PROGRESS_ARROW;

        private final Map<RecipeCapability<?>, CapabilityUIInfo> capabilityInfo = new Object2ObjectOpenHashMap<>();
        private final GTRecipeType recipeType;
        private final List<RecipeUIModifier> recipeUIModifiers = new ObjectArrayList<>();
        private @Nullable ProgressWidgetSupplier progressWidgetSupplier = null;

        private @Nullable Function<GTRecipe, Flow> customUIBuilder;

        public Builder(GTRecipeType recipeType) {
            this.recipeType = recipeType;

            // Setup defaults

            getCapInfo(ItemRecipeCapability.CAP).machineLayoutBuilder = MachineCapabilityLayoutBuilder.ITEM;
            getCapInfo(FluidRecipeCapability.CAP).machineLayoutBuilder = MachineCapabilityLayoutBuilder.FLUID;

            getCapInfo(ItemRecipeCapability.CAP).recipeViewerLayoutBuilder = RecipeViewerCapabilityLayoutBuilder.ITEM;
            getCapInfo(FluidRecipeCapability.CAP).recipeViewerLayoutBuilder = RecipeViewerCapabilityLayoutBuilder.FLUID;
            getCapInfo(
                    CWURecipeCapability.CAP).recipeViewerLayoutBuilder = RecipeViewerCapabilityLayoutBuilder.COMPUTATION;
            getCapInfo(EURecipeCapability.CAP).recipeViewerLayoutBuilder = RecipeViewerCapabilityLayoutBuilder.EU;

            getCapInfo(ItemRecipeCapability.CAP).capabilityWidgetBuilder = CapabilityContentBuilder.ITEM;
            getCapInfo(FluidRecipeCapability.CAP).capabilityWidgetBuilder = CapabilityContentBuilder.FLUID;
            getCapInfo(CWURecipeCapability.CAP).capabilityWidgetBuilder = CapabilityContentBuilder.COMPUTATION;
            getCapInfo(EURecipeCapability.CAP).capabilityWidgetBuilder = CapabilityContentBuilder.EU;
        }

        private CapabilityUIInfo getCapInfo(RecipeCapability<?> cap) {
            return capabilityInfo.computeIfAbsent(cap, CapabilityUIInfo::new);
        }

        /**
         * Adds a slot overlay for a specific slot
         *
         * @param ioMode    The IO of the slot
         * @param slotIndex The index of the slot.
         * @param cap       The slot capability
         * @param overlay   The slot overlay.
         */
        public Builder setSlotOverlay(IO ioMode, int slotIndex, RecipeCapability<?> cap, IDrawable overlay) {
            getCapInfo(cap).overlays.computeIfAbsent(ioMode, $ -> new Int2ObjectOpenHashMap<>())
                    .put(slotIndex, overlay);
            return this;
        }

        /**
         * Adds a slot overlay for an item slot
         *
         * @param ioMode    The IO of the slot
         * @param slotIndex The index of the slot.
         * @param overlay   The slot overlay.
         */
        public Builder setItemSlotOverlay(IO ioMode, int slotIndex, IDrawable overlay) {
            return setSlotOverlay(ioMode, slotIndex, ItemRecipeCapability.CAP, overlay);
        }

        /**
         * Adds a slot overlay for a fluid slot
         *
         * @param ioMode    The IO of the slot
         * @param slotIndex The index of the slot.
         * @param overlay   The slot overlay.
         */
        public Builder setFluidSlotOverlay(IO ioMode, int slotIndex, IDrawable overlay) {
            return setSlotOverlay(ioMode, slotIndex, FluidRecipeCapability.CAP, overlay);
        }

        /**
         * Adds a slot overlay for multiple item slots.
         *
         * @param ioMode         The IO of the slot
         * @param slotIndexStart The first item slot to add the overlay to.
         * @param slotIndexEnd   The last item slot to add the overlay to.
         * @param overlay        The slot overlay.
         */
        public Builder setItemSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd, IDrawable overlay) {
            for (int i = slotIndexStart; i <= slotIndexEnd; i++) {
                setSlotOverlay(ioMode, i, ItemRecipeCapability.CAP, overlay);
            }
            return this;
        }

        /**
         * Adds a slot overlay for multiple fluid slots.
         *
         * @param ioMode         The IO of the slot
         * @param slotIndexStart The first fluid slot to add the overlay to.
         * @param slotIndexEnd   The last fluid slot to add the overlay to.
         * @param overlay        The slot overlay.
         */
        public Builder setFluidSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd, IDrawable overlay) {
            for (int i = slotIndexStart; i <= slotIndexEnd; i++) {
                setSlotOverlay(ioMode, i, FluidRecipeCapability.CAP, overlay);
            }
            return this;
        }

        /**
         * Sets the progress bar texture
         * 
         * @param progressBar {@link ProgressBarTextureSet}, which holds progress texture info.
         */
        public Builder setProgressBar(ProgressBarTextureSet progressBar) {
            this.progressBar = progressBar;
            return this;
        }

        /**
         * Sets a supplier which returns a widget to be used as a progress bar
         * 
         * @param progressBarSupplier Progress widget supplier
         */
        public Builder setProgressBarSupplier(ProgressWidgetSupplier progressBarSupplier) {
            this.progressWidgetSupplier = progressBarSupplier;
            return this;
        }

        /**
         * For singleblock machines using this recipe type, sets a function that builds the ui for a specific capability
         * type.
         *
         * @param cap     The capability type
         * @param builder UI builder.
         */
        public Builder setMachineCapabilityLayoutBuilder(RecipeCapability<?> cap,
                                                         MachineCapabilityLayoutBuilder builder) {
            getCapInfo(cap).machineLayoutBuilder = builder;
            return this;
        }

        /**
         * For singleblock machines using this recipe type, sets a function that builds the slot grid layout.
         *
         * @param cap         The capability type
         * @param gridBuilder Function that returns a {@code String[]}, where 's' should be used to denote a slot.
         */
        public Builder setMachineLayoutGridBuilder(RecipeCapability<?> cap, IO io,
                                                   MachineCapabilityGridBuilder gridBuilder) {
            getCapInfo(cap).machineLayoutGridBuilders.put(io, gridBuilder);
            return this;
        }

        /**
         * Loads a recipe type UI from a file.<br>
         * <b>Loading a recipe type UI from a file will override some other builder options.</b>
         *
         * @param fileName Filename
         */
        public Builder loadRecipeTypeUIFromFile(String fileName) {
            throw new NotImplementedException();
        }

        /**
         * Sets a custom function which returns a flow to be used as the recipe viewer UI.<br>
         * <b>Using a custom UI builder will override some other builder options.</b>
         * 
         * @param customUIBuilder Custom UI builder
         */
        public Builder customRecipeTypeUI(Function<GTRecipe, Flow> customUIBuilder) {
            this.customUIBuilder = customUIBuilder;
            return this;
        }

        /**
         * For the recipe viewer UI, sets a function that builds the ui for a specific capability type.
         *
         * @param cap     The capability type
         * @param builder UI builder.
         */
        public Builder setRecipeViewerLayoutCapabilityLayoutBuilder(RecipeCapability<?> cap,
                                                                    RecipeViewerCapabilityLayoutBuilder builder) {
            getCapInfo(cap).recipeViewerLayoutBuilder = builder;
            return this;
        }

        /**
         * For the recipe viewer UI, sets a function that builds the slot grid layout.
         *
         * @param cap         The capability type
         * @param gridBuilder Function that returns a {@code String[]}, where 's' should be used to denote a slot.
         */
        public Builder setRecipeViewerLayoutGridBuilder(RecipeCapability<?> cap, IO io,
                                                        RecipeViewerCapabilityGridBuilder gridBuilder) {
            getCapInfo(cap).recipeViewerLayoutGridBuilders.put(io, gridBuilder);
            return this;
        }

        /**
         * Sets a function that builds the slot grid layout for both machine and recipe viewer UI
         * 
         * @param cap         The capability
         * @param io          IO
         * @param gridBuilder Function that returns a {@code String[]}, where 's' should be used to denote a slot.
         */
        public Builder setLayoutGridBuilder(RecipeCapability<?> cap, IO io,
                                            Function<GTRecipeTypeUILayout, String[]> gridBuilder) {
            setRecipeViewerLayoutGridBuilder(cap, io, gridBuilder::apply);
            setMachineLayoutGridBuilder(cap, io, (m, l) -> gridBuilder.apply(l));
            return this;
        }

        /**
         * Adds a {@link RecipeUIModifier}, which modifies the recipe viewer UI after creation. Useful for adding extra
         * information to recipe viewer recipes.
         * 
         * @param recipeUIModifier Recipe UI modifier.
         * @see RecipeUIModifier
         */
        public Builder addRecipeUIModifier(RecipeUIModifier recipeUIModifier) {
            recipeUIModifiers.add(recipeUIModifier);
            return this;
        }

        /**
         * Defines a function used to map recipe contents to their slots.<br>
         * This should only be used for custom capabilities.
         * 
         * @param cap            Recipe capability
         * @param contentBuilder Capability content builder
         */
        public Builder setCapabilityContentBuilder(RecipeCapability<?> cap, CapabilityContentBuilder contentBuilder) {
            getCapInfo(cap).capabilityWidgetBuilder = contentBuilder;
            return this;
        }

        public GTRecipeTypeUILayout build() {
            var progressWidgetSupplier = this.progressWidgetSupplier;
            if (progressWidgetSupplier == null) progressWidgetSupplier = (l, v, m) -> new ProgressWidget()
                    .value(v)
                    .name("progressBar")
                    .texture(progressBar.get(m), progressBar.progressSize())
                    .size(progressBar.progressSize())
                    .direction(progressBar.fillDirection());

            var layout = new GTRecipeTypeUILayout(recipeType, capabilityInfo, recipeUIModifiers, progressWidgetSupplier,
                    customUIBuilder);
            layout.progressBar = progressBar;
            return layout;
        }
    }
}
