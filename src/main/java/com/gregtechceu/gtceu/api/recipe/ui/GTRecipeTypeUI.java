package com.gregtechceu.gtceu.api.recipe.ui;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.editor.IEditableUI;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeTypeEmiCategory;
import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeTypeCategory;
import com.gregtechceu.gtceu.integration.rei.recipe.GTRecipeTypeDisplayCategory;
import com.gregtechceu.gtceu.utils.OverlayingFluidStorage;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.editor.data.Resources;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.JEIPlugin;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.emi.emi.api.EmiApi;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import lombok.Getter;
import lombok.Setter;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.DoubleSupplier;

public class GTRecipeTypeUI {

    @Getter
    @Setter
    private Byte2ObjectMap<IGuiTexture> slotOverlays = new Byte2ObjectArrayMap<>();

    private final GTRecipeType recipeType;

    @Getter
    @Setter
    private ProgressTexture progressBarTexture = new ProgressTexture(GuiTextures.PROGRESS_BAR_ARROW.getSubTexture(0, 0, 1, 0.5), GuiTextures.PROGRESS_BAR_ARROW.getSubTexture(0, 0.5, 1, 0.5));
    @Setter
    private SteamTexture steamProgressBarTexture = null;
    @Setter
    private ProgressTexture.FillDirection steamMoveType = ProgressTexture.FillDirection.LEFT_TO_RIGHT;
    @Setter
    @Nullable
    protected BiConsumer<GTRecipe, WidgetGroup> uiBuilder;
    @Setter
    @Getter
    protected int maxTooltips = 3;

    @Getter
    @Setter
    private boolean JEIVisible = true;

    private CompoundTag customUICache;

    /**
     * @param recipeType the recipemap corresponding to this ui
     */
    public GTRecipeTypeUI(@NotNull GTRecipeType recipeType) {
        this.recipeType = recipeType;
    }

    public CompoundTag getCustomUI() {
        if (this.customUICache == null) {
            ResourceManager resourceManager = null;
            if (LDLib.isClient()) {
                resourceManager = Minecraft.getInstance().getResourceManager();
            } else if (Platform.getMinecraftServer() != null) {
                resourceManager = Platform.getMinecraftServer().getResourceManager();
            }
            if (resourceManager == null) {
                this.customUICache = new CompoundTag();
            } else {
                try {
                    var resource = resourceManager.getResourceOrThrow(new ResourceLocation(recipeType.registryName.getNamespace(), "ui/recipe_type/%s.rtui".formatted(recipeType.registryName.getPath())));
                    try (InputStream inputStream = resource.open()){
                        try (DataInputStream dataInputStream = new DataInputStream(inputStream);){
                            this.customUICache = NbtIo.read(dataInputStream, NbtAccounter.UNLIMITED);
                        }
                    }
                } catch (Exception e) {
                    this.customUICache = new CompoundTag();
                }
                if (this.customUICache == null) {
                    this.customUICache = new CompoundTag();
                }
            }
        }
        return this.customUICache;
    }

    public boolean hasCustomUI() {
        return !getCustomUI().isEmpty();
    }

    public void reloadCustomUI() {
        this.customUICache = null;
    }

    public Size getJEISize() {
        Size size = createEditableUITemplate(false, false).createDefault().getSize();
        return new Size(size.width, getPropertyHeightShift() + 5 + size.height);
    }

    public record RecipeHolder(DoubleSupplier progressSupplier, IItemTransfer importItems, IItemTransfer exportItems, IFluidTransfer importFluids, IFluidTransfer exportFluids, boolean isSteam, boolean isHighPressure) {};

    /**
     * Auto layout UI template for recipes.
     * @param progressSupplier progress. To create a JEI / REI UI, use the para {@link ProgressWidget#JEIProgress}.
     */
    public WidgetGroup createUITemplate(DoubleSupplier progressSupplier, IItemTransfer importItems, IItemTransfer exportItems, IFluidTransfer importFluids, IFluidTransfer exportFluids, boolean isSteam, boolean isHighPressure) {
        var template = createEditableUITemplate(isSteam, isHighPressure);
        var group = template.createDefault();
        template.setupUI(group, new RecipeHolder(progressSupplier, importItems, exportItems, importFluids, exportFluids, isSteam, isHighPressure));
        return group;
    }

    public WidgetGroup createUITemplate(DoubleSupplier progressSupplier, IItemTransfer importItems, IItemTransfer exportItems, IFluidTransfer importFluids, IFluidTransfer exportFluids) {
        return createUITemplate(progressSupplier, importItems, exportItems, importFluids, exportFluids, false, false);
    }

    /**
     * Auto layout UI template for recipes.
     */
    public IEditableUI<WidgetGroup, RecipeHolder> createEditableUITemplate(final boolean isSteam, final boolean isHighPressure) {
        return new IEditableUI.Normal<>(() -> {
            var isCustomUI = !isSteam && hasCustomUI();
            if (isCustomUI) {
                CompoundTag nbt = getCustomUI();
                WidgetGroup group = new WidgetGroup();
                IConfigurableWidget.deserializeNBT(group, nbt.getCompound("root"), Resources.fromNBT(nbt.getCompound("resources")), false);
                group.setSelfPosition(new Position(0, 0));
                return group;
            }

            WidgetGroup inputs = addInventorySlotGroup(false, isSteam, isHighPressure);
            WidgetGroup outputs = addInventorySlotGroup(true, isSteam, isHighPressure);
            WidgetGroup group = new WidgetGroup(new Position(0, 0));

            group.addWidget(inputs);
            group.addWidget(outputs);

            ProgressWidget progressWidget = new ProgressWidget(ProgressWidget.JEIProgress, 78, 23, 20, 20, progressBarTexture);
            progressWidget.setId("progress");
            group.addWidget(progressWidget);

            progressWidget.setProgressTexture((isSteam && steamProgressBarTexture != null) ? new ProgressTexture(
                steamProgressBarTexture.get(isHighPressure).getSubTexture(0, 0, 1, 0.5),
                steamProgressBarTexture.get(isHighPressure).getSubTexture(0, 0.5, 1, 0.5))
                .setFillDirection(steamMoveType)
                : progressBarTexture);

            group.setSize(new Size(Math.max(group.getSize().width, 176), group.getSize().height));
            return group;
        }, (template, recipeHolder) -> {
            var isJEI = recipeHolder.progressSupplier == ProgressWidget.JEIProgress;

            // bind progress
            List<Widget> progress = new ArrayList<>();
            WidgetUtils.widgetByIdForEach(template, "^progress$", ProgressWidget.class, progressWidget -> {
                progressWidget.setProgressSupplier(recipeHolder.progressSupplier);
                progress.add(progressWidget);
            });
            // add recipe button
            if (!isJEI && (LDLib.isReiLoaded() || LDLib.isJeiLoaded() || LDLib.isEmiLoaded())) {
                for (Widget widget : progress) {
                    template.addWidget(new ButtonWidget(widget.getPosition().x, widget.getPosition().y, widget.getSize().width, widget.getSize().height, IGuiTexture.EMPTY, cd -> {
                        if (cd.isRemote) {
                            if (LDLib.isReiLoaded()) {
                                ViewSearchBuilder.builder().addCategory(GTRecipeTypeDisplayCategory.CATEGORIES.apply(recipeType)).open();
                            } else if (LDLib.isJeiLoaded()) {
                                JEIPlugin.jeiRuntime.getRecipesGui().showTypes(List.of(GTRecipeTypeCategory.TYPES.apply(recipeType)));
                            } else if (LDLib.isEmiLoaded()) {
                                EmiApi.displayRecipeCategory(GTRecipeTypeEmiCategory.CATEGORIES.apply(recipeType));
                            }
                        }
                    }).setHoverTooltips("gtceu.recipe_type.show_recipes"));
                }
            }
            // bind item in
            WidgetUtils.widgetByIdForEach(template, "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(IO.IN)), SlotWidget.class, slot -> {
                var index = WidgetUtils.widgetIdIndex(slot);
                if (index >= 0 && index < recipeHolder.importItems.getSlots()) {
                    slot.setHandlerSlot(recipeHolder.importItems, index);
                    slot.setIngredientIO(IngredientIO.INPUT);
                    slot.setCanTakeItems(!isJEI);
                    slot.setCanPutItems(!isJEI);
                }
            });
            // bind item out
            WidgetUtils.widgetByIdForEach(template, "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(IO.OUT)), SlotWidget.class, slot -> {
                var index = WidgetUtils.widgetIdIndex(slot);
                if (index >= 0 && index < recipeHolder.exportItems.getSlots()) {
                    slot.setHandlerSlot(recipeHolder.exportItems, index);
                    slot.setIngredientIO(IngredientIO.OUTPUT);
                    slot.setCanTakeItems(!isJEI);
                    slot.setCanPutItems(false);
                }
            });
            // bind fluid in
            WidgetUtils.widgetByIdForEach(template, "^%s_[0-9]+$".formatted(FluidRecipeCapability.CAP.slotName(IO.IN)), TankWidget.class, tank -> {
                var index = WidgetUtils.widgetIdIndex(tank);
                if (index >= 0 && index < recipeHolder.importFluids.getTanks()) {
                    tank.setFluidTank(new OverlayingFluidStorage(recipeHolder.importFluids, index));
                    tank.setIngredientIO(IngredientIO.INPUT);
                    tank.setAllowClickFilled(!isJEI);
                    tank.setAllowClickDrained(!isJEI);
                }
            });
            // bind fluid out
            WidgetUtils.widgetByIdForEach(template, "^%s_[0-9]+$".formatted(FluidRecipeCapability.CAP.slotName(IO.OUT)), TankWidget.class, tank -> {
                var index = WidgetUtils.widgetIdIndex(tank);
                if (index >= 0 && index < recipeHolder.exportFluids.getTanks()) {
                    tank.setFluidTank(new OverlayingFluidStorage(recipeHolder.exportFluids, index));
                    tank.setIngredientIO(IngredientIO.OUTPUT);
                    tank.setAllowClickFilled(!isJEI);
                    tank.setAllowClickDrained(false);
                }
            });
        });
    }

    protected WidgetGroup addInventorySlotGroup(boolean isOutputs, boolean isSteam, boolean isHighPressure) {
        var itemCountOriginal = isOutputs ? recipeType.getMaxOutputs(ItemRecipeCapability.CAP) : recipeType.getMaxInputs(ItemRecipeCapability.CAP);
        var fluidCountOriginal = isOutputs ? recipeType.getMaxOutputs(FluidRecipeCapability.CAP) : recipeType.getMaxInputs(FluidRecipeCapability.CAP);
        int itemCount = itemCountOriginal;
        int fluidCount = fluidCountOriginal;
        boolean invertFluids = false;
        if (itemCount == 0) {
            int tmp = itemCount;
            itemCount = fluidCount;
            fluidCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 70 - itemSlotsToLeft * 18;
        int startInputsY = 33 - (int) (itemSlotsToDown / 2.0 * 18);
        boolean wasGroup = itemCountOriginal + fluidCountOriginal == 12;
        if (wasGroup) startInputsY -= 9;
        else if (itemCountOriginal >= 6 && fluidCountOriginal >= 2 && !isOutputs) startInputsY -= 9;

        WidgetGroup group = new WidgetGroup(new Position(startInputsX, startInputsY));

        int count = invertFluids ? fluidCountOriginal : itemCountOriginal;
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                if (slotIndex >= itemCount) break;
                int x = 18 * j;
                int y = 18 * i;
                addSlot(group, x, y, slotIndex, count, invertFluids, isOutputs, isSteam, isHighPressure);
            }
        }
        int offset = wasGroup ? 2 : 0;
        if (fluidCount > 0 || invertFluids) {
            count = invertFluids ? itemCountOriginal : fluidCountOriginal;
            if (itemSlotsToDown >= fluidCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? itemSlotsToLeft * 18 : -18;
                for (int i = 0; i < fluidCount; i++) {
                    int y = offset + 18 * i;
                    addSlot(group, startSpecX, y, i, count, !invertFluids, isOutputs, isSteam, isHighPressure);
                }
            } else {
                int startSpecY = itemSlotsToDown * 18;
                for (int i = 0; i < fluidCount; i++) {
                    int x = isOutputs ? 18 * (i % 3) :
                        itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(group, x, y, i, count, !invertFluids, isOutputs, isSteam, isHighPressure);
                }
            }
        }
        return group;
    }

    /**
     * Add a slot to this ui
     */
    protected void addSlot(WidgetGroup group, int x, int y, int slotIndex, int count, boolean isFluid, boolean isOutputs, boolean isSteam, boolean isHighPressure) {
        if (!isFluid) {
            var slot = new SlotWidget();
            slot.initTemplate();
            slot.setSelfPosition(new Position(x, y));
            slot.setBackground(getOverlaysForSlot(isOutputs, false, slotIndex == count - 1, isSteam, isHighPressure));
            slot.setId(ItemRecipeCapability.CAP.slotName(isOutputs ? IO.OUT : IO.IN, slotIndex));
            group.addWidget(slot);
        } else {
            var tank = new TankWidget();
            tank.initTemplate();
            tank.setFillDirection(ProgressTexture.FillDirection.ALWAYS_FULL);
            tank.setSelfPosition(new Position(x, y));
            tank.setBackground(getOverlaysForSlot(isOutputs, true, slotIndex == count - 1, isSteam, isHighPressure));
            tank.setId(FluidRecipeCapability.CAP.slotName(isOutputs ? IO.OUT : IO.IN, slotIndex));
            group.addWidget(tank);
        }
    }

    protected static int[] determineSlotsGrid(int itemCount) {
        int itemSlotsToLeft;
        int itemSlotsToDown;
        double sqrt = Math.sqrt(itemCount);
        //if the number of input has an integer root
        //return it.
        if (sqrt % 1 == 0) {
            itemSlotsToLeft = itemSlotsToDown = (int) sqrt;
        } else if (itemCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            //if we couldn't fit all into a perfect square,
            //increase the amount of slots to the left
            itemSlotsToLeft = (int) Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            //if we still can't fit all the slots in a grid,
            //increase the amount of slots on the bottom
            if (itemCount > itemSlotsToLeft * itemSlotsToDown) {
                itemSlotsToDown = itemSlotsToLeft;
            }
        }
        return new int[]{itemSlotsToLeft, itemSlotsToDown};
    }


    protected IGuiTexture getOverlaysForSlot(boolean isOutput, boolean isFluid, boolean isLast, boolean isSteam, boolean isHighPressure) {
        IGuiTexture base = isFluid ? GuiTextures.FLUID_SLOT : (isSteam ? GuiTextures.SLOT_STEAM.get(isHighPressure) : GuiTextures.SLOT);
        byte overlayKey = (byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0));
        if (slotOverlays.containsKey(overlayKey)) {
            return new GuiTextureGroup(base, slotOverlays.get(overlayKey));
        }
        return base;
    }

    /**
     * @return the height used to determine size of background texture in JEI
     */
    public int getPropertyHeightShift() {
        int maxPropertyCount = recipeType.getMaxTooltips() + recipeType.getDataInfos().size();
        return maxPropertyCount * 10; // GTRecipeWrapper#LINE_HEIGHT
    }

    public void appendJEIUI(GTRecipe recipe, WidgetGroup widgetGroup) {
        if (uiBuilder != null) {
            uiBuilder.accept(recipe, widgetGroup);
        }
    }

    public GTRecipeTypeUI setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true, slotOverlay);
    }

    public GTRecipeTypeUI setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, IGuiTexture slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public GTRecipeTypeUI setProgressBar(ResourceTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.progressBarTexture = new ProgressTexture(progressBar.getSubTexture(0, 0, 1, 0.5), progressBar.getSubTexture(0, 0.5, 1, 0.5)).setFillDirection(moveType);
        return this;
    }
}
