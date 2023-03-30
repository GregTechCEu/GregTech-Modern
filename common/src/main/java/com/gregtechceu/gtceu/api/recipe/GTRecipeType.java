package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.core.mixins.RecipeManagerInvoker;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
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
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Rect;
import com.lowdragmc.lowdraglib.utils.Size;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.*;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipeType
 */
@Accessors(chain = true)
public class GTRecipeType implements RecipeType<GTRecipe> {
    public final ResourceLocation registryName;
    public final String group;
    public final Object2IntMap<RecipeCapability<?>> maxInputs = new Object2IntOpenHashMap<>();
    public final Object2IntMap<RecipeCapability<?>> maxOutputs = new Object2IntOpenHashMap<>();
    @Setter
    private GTRecipeBuilder recipeBuilder;
    @Setter
    private ProgressTexture progressBarTexture = new ProgressTexture();
    @Setter
    private SteamTexture steamProgressBarTexture = null;
    private ProgressTexture.FillDirection steamMoveType = ProgressTexture.FillDirection.LEFT_TO_RIGHT;
    private IGuiTexture specialTexture;
    private Rect specialTexturePosition;
    private final Byte2ObjectMap<IGuiTexture> slotOverlays = new Byte2ObjectArrayMap<>();
    @Setter
    @Getter
    private GTRecipeType smallRecipeMap;
    @Setter
    @Getter
    private Supplier<ItemStack> iconSupplier;
    @Nullable
    @Setter
    @Getter
    protected SoundEntry sound;
    @Getter
    protected List<Function<CompoundTag, String>> dataInfos = new ArrayList<>();
    @Setter
    @Nullable
    protected BiConsumer<GTRecipe, WidgetGroup> uiBuilder;
    @Setter
    @Getter
    protected boolean isFuelRecipeType;
    @Getter
    protected final Map<RecipeType<?>, List<GTRecipe>> proxyRecipes;
    private CompoundTag customUICache;

    public GTRecipeType(ResourceLocation registryName, String group, RecipeType<?>... proxyRecipes) {
        this.registryName = registryName;
        this.group = group;
        recipeBuilder = new GTRecipeBuilder(registryName, this);
        // must be linked to stop json contents from shuffling
        Map<RecipeType<?>, List<GTRecipe>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (RecipeType<?> proxyRecipe : proxyRecipes) {
            map.put(proxyRecipe, new ArrayList<>());
        }
        this.proxyRecipes = map;
    }

    public GTRecipeType setMaxIOSize(int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs) {
        return setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxInputs).setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
                .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxOutputs).setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
    }

    public GTRecipeType setEUIO(IO io) {
        if (io.support(IO.IN)) {
            setMaxSize(IO.IN, EURecipeCapability.CAP, 1);
        }
        if (io.support(IO.OUT)) {
            setMaxSize(IO.OUT, EURecipeCapability.CAP, 1);
        }
        return this;
    }

    public GTRecipeType setMaxSize(IO io, RecipeCapability<?> cap, int max) {
        if (io == IO.IN || io == IO.BOTH) {
            maxInputs.put(cap, max);
        }
        if (io == IO.OUT || io == IO.BOTH) {
            maxOutputs.put(cap, max);
        }
        return this;
    }

    public GTRecipeType setSpecialTexture(int x, int y, int width, int height, IGuiTexture area) {
        this.specialTexturePosition = Rect.of(new Position(x, y), new Size(width, height));
        this.specialTexture = area;
        return this;
    }

    public GTRecipeType setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true, slotOverlay);
    }

    public GTRecipeType setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, IGuiTexture slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public GTRecipeType setProgressBar(ResourceTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.progressBarTexture = new ProgressTexture(progressBar.getSubTexture(0, 0, 1, 0.5), progressBar.getSubTexture(0, 0.5, 1, 0.5)).setFillDirection(moveType);
        return this;
    }

    public GTRecipeType setSteamProgressBar(SteamTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.steamProgressBarTexture = progressBar;
        this.steamMoveType = moveType;
        return this;
    }

    public GTRecipeType addDataInfo(Function<CompoundTag, String> dataInfo) {
        this.dataInfos.add(dataInfo);
        return this;
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    @Nullable
    public GTRecipe getRecipe(RecipeManager recipeManager, ResourceLocation id) {
        var recipes = ((RecipeManagerInvoker) recipeManager).getRecipeFromType(this);
        if (recipes.get(id) instanceof GTRecipe recipe) {
            return recipe;
        }
        return null;
    }

    public List<GTRecipe> searchFuelRecipe(RecipeManager recipeManager, IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies() || !isFuelRecipeType()) return Collections.emptyList();
        List<GTRecipe> matches = new ArrayList<>();
        for (GTRecipe recipe : recipeManager.getAllRecipesFor(this)) {
            if (recipe.isFuel && recipe.matchRecipe(holder) && recipe.matchTickRecipe(holder)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    public List<GTRecipe> searchRecipe(RecipeManager recipeManager, IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies()) return Collections.emptyList();
        List<GTRecipe> matches = new ArrayList<>();
        for (var recipe : recipeManager.getAllRecipesFor(this)) {
            if (!recipe.isFuel && recipe.matchRecipe(holder) && recipe.matchTickRecipe(holder)) {
                matches.add(recipe);
            }
        }
        for (List<GTRecipe> recipes : proxyRecipes.values()) {
            for (GTRecipe recipe : recipes) {
                if (!recipe.isFuel && recipe.matchRecipe(holder) && recipe.matchTickRecipe(holder)) {
                    matches.add(recipe);
                }
            }
        }
        return matches;
    }

    public int getMaxInputs(RecipeCapability<?> cap) {
        return maxInputs.getOrDefault(cap, 0);

    }

    public int getMaxOutputs(RecipeCapability<?> cap) {
        return maxOutputs.getOrDefault(cap, 0);
    }

    //////////////////////////////////////
    //*****     Recipe Builder    ******//
    //////////////////////////////////////

    public GTRecipeType prepareBuilder(Consumer<GTRecipeBuilder> onPrepare) {
        onPrepare.accept(recipeBuilder);
        return this;
    }

    public GTRecipeBuilder recipeBuilder(ResourceLocation id, Object... append) {
        if (append.length > 0) {
            return recipeBuilder.copy(new ResourceLocation(id.getNamespace(),
                    id.getPath() + Arrays.stream(append).map(Object::toString).map(FormattingUtil::toLowerCaseUnder).reduce("", (a, b) -> a + "_" + b)));
        }
        return recipeBuilder.copy(id);
    }

    public GTRecipeBuilder recipeBuilder(String id, Object... append) {
        return recipeBuilder(GTCEu.id(id), append);
    }

    public GTRecipeBuilder recipeBuilder(UnificationEntry entry, Object... append) {
        return recipeBuilder(GTCEu.id(entry.tagPrefix + (entry.material == null ? "" : "_" + entry.material)), append);
    }

    public GTRecipeBuilder recipeBuilder(Supplier<? extends ItemLike> item, Object... append) {
        return recipeBuilder(item.get(), append);
    }

    public GTRecipeBuilder recipeBuilder(ItemLike itemLike, Object... append) {
        return recipeBuilder(new ResourceLocation(itemLike.asItem().getDescriptionId()), append);
    }

    public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return recipeBuilder.copyFrom(builder);
    }

    public GTRecipeType onRecipeBuild(BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onBuild) {
        recipeBuilder.onSave(onBuild);
        return this;
    }

    //////////////////////////////////////
    //***********     UI    ************//
    //////////////////////////////////////

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
                    var resource = resourceManager.getResourceOrThrow(new ResourceLocation(registryName.getNamespace(), "ui/recipe_type/%s.rtui".formatted(registryName.getPath())));
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
        return new Size(176, (dataInfos.size() + 3) * 10 + 5 + createDefaultUITemplate(true).getSize().height);
    }

    /**
     * Auto layout UI template for recipes.
     * @param progressSupplier progress. To create a JEI / REI UI, use the para {@link ProgressWidget#JEIProgress}.
     */
    public WidgetGroup createUITemplate(DoubleSupplier progressSupplier, IItemTransfer importItems, IItemTransfer exportItems, IFluidStorage[] importFluids, IFluidStorage[] exportFluids, boolean isSteam, boolean isHighPressure) {
        var isJEI = progressSupplier == ProgressWidget.JEIProgress;
        var useCustomUI = !isSteam && hasCustomUI();
        WidgetGroup group  = createDefaultUITemplate(useCustomUI);

        // bind progress
        WidgetUtils.widgetByIdForEach(group, "^progress$", ProgressWidget.class, progressWidget -> {
            progressWidget.setProgressSupplier(progressSupplier);
            if (!useCustomUI) {
                progressWidget.setProgressBar((isSteam && steamProgressBarTexture != null) ? new ProgressTexture(
                        steamProgressBarTexture.get(isHighPressure).getSubTexture(0, 0, 1, 0.5),
                        steamProgressBarTexture.get(isHighPressure).getSubTexture(0, 0.5, 1, 0.5))
                        .setFillDirection(steamMoveType)
                        : progressBarTexture);
            }
        });
        // bind item in
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(IO.IN)), SlotWidget.class, slot -> {
           var index = WidgetUtils.widgetIdIndex(slot);
           if (index >= 0 && index < importItems.getSlots()) {
               slot.setHandlerSlot(importItems, index);
               slot.setCanTakeItems(!isJEI);
               slot.setCanPutItems(!isJEI);
               if (!useCustomUI) {
                   slot.setBackground(getOverlaysForSlot(false, false, index == importItems.getSlots() - 1, isSteam, isHighPressure));
               }
           }
        });
        // bind item out
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(IO.OUT)), SlotWidget.class, slot -> {
            var index = WidgetUtils.widgetIdIndex(slot);
            if (index >= 0 && index < exportItems.getSlots()) {
                slot.setHandlerSlot(exportItems, index);
                slot.setCanTakeItems(!isJEI);
                slot.setCanPutItems(false);
                if (!useCustomUI) {
                    slot.setBackground(getOverlaysForSlot(true, false, index == exportItems.getSlots() - 1, isSteam, isHighPressure));
                }
              }
        });
        // bind fluid in
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(FluidRecipeCapability.CAP.slotName(IO.IN)), TankWidget.class, tank -> {
            var index = WidgetUtils.widgetIdIndex(tank);
            if (index >= 0 && index < importFluids.length) {
                tank.setFluidTank(importFluids[index]);
                tank.setAllowClickFilled(!isJEI);
                tank.setAllowClickDrained(!isJEI);
                if (!useCustomUI) {
                    tank.setBackground(getOverlaysForSlot(false, true, index == importFluids.length - 1, isSteam, isHighPressure));
                }
            }
        });
        // bind fluid out
        WidgetUtils.widgetByIdForEach(group, "^%s_[0-9]+$".formatted(FluidRecipeCapability.CAP.slotName(IO.OUT)), TankWidget.class, tank -> {
            var index = WidgetUtils.widgetIdIndex(tank);
            if (index >= 0 && index < exportFluids.length) {
                tank.setFluidTank(exportFluids[index]);
                tank.setAllowClickFilled(!isJEI);
                tank.setAllowClickDrained(false);
                if (!useCustomUI) {
                    tank.setBackground(getOverlaysForSlot(true, true, index == exportFluids.length - 1, isSteam, isHighPressure));
                }
            }
        });
        return group;
    }

    /**
     * Auto layout UI template for recipes.
     */
    public WidgetGroup createDefaultUITemplate(boolean useCustomUI) {
        if (useCustomUI && hasCustomUI()) {
            var nbt = getCustomUI();
            var group = new WidgetGroup();
            IConfigurableWidget.deserializeNBT(group, nbt.getCompound("root"), Resources.fromNBT(nbt.getCompound("resources")), false);
            group.setSelfPosition(new Position(0, 0));
            return group;
        }

        var inputs = addInventorySlotGroup(false);
        var outputs = addInventorySlotGroup(true);
        var group = new WidgetGroup(0, 0, 176, Math.max(inputs.getSize().height, outputs.getSize().height));
        var size = group.getSize();

        inputs.addSelfPosition(size.width / 2 - 20 - inputs.getSize().width, (size.height - inputs.getSize().height) / 2);
        outputs.addSelfPosition(size.width / 2 + 20, (size.height - outputs.getSize().height) / 2);
        group.addWidget(inputs);
        group.addWidget(outputs);

        var progressWidget = new ProgressWidget(ProgressWidget.JEIProgress, size.width / 2 - 10, size.height / 2 - 10, 20, 20, progressBarTexture);
        progressWidget.setId("progress");
        group.addWidget(progressWidget);

        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(group);
        return group;
    }

    public WidgetGroup createUITemplate(DoubleSupplier progressSupplier, IItemTransfer importItems, IItemTransfer exportItems, IFluidStorage[] importFluids, IFluidStorage[] exportFluids) {
        return createUITemplate(progressSupplier, importItems, exportItems, importFluids, exportFluids, false, false);
    }

    protected void addSpecialTexture(WidgetGroup group) {
        group.addWidget(new ImageWidget(specialTexturePosition.left, specialTexturePosition.up, specialTexturePosition.getWidth(), specialTexturePosition.getHeight(), specialTexture));
    }

    protected WidgetGroup addInventorySlotGroup(boolean isOutputs) {
        var itemCount = isOutputs ? getMaxOutputs(ItemRecipeCapability.CAP) : getMaxInputs(ItemRecipeCapability.CAP);
        var fluidCount = isOutputs ? getMaxOutputs(FluidRecipeCapability.CAP) : getMaxInputs(FluidRecipeCapability.CAP);
        var sum = itemCount + fluidCount;
        WidgetGroup group = new WidgetGroup(0, 0, Math.min(sum, 3) * 18 + 8, ((sum + 2) / 3) * 18 + 8);
        int index = 0;
        for (int slotIndex = 0; slotIndex < itemCount; slotIndex++) {
            var slot = new SlotWidget();
            slot.setSelfPosition(new Position((index % 3) * 18 + 4, (index / 3) * 18 + 4));
            slot.setIngredientIO(isOutputs ? IngredientIO.OUTPUT : IngredientIO.INPUT);
            slot.setBackground(getOverlaysForSlot(isOutputs, false, slotIndex == itemCount - 1, false, false));
            slot.setId(ItemRecipeCapability.CAP.slotName(isOutputs ? IO.OUT : IO.IN, slotIndex));
            group.addWidget(slot);
            index++;
        }
        for (int i = 0; i < fluidCount; i++) {
            var tank = new TankWidget();
            tank.setSelfPosition(new Position((index % 3) * 18 + 4, (index / 3) * 18 + 4));
            tank.setIngredientIO(isOutputs ? IngredientIO.OUTPUT : IngredientIO.INPUT);
            tank.setBackground(getOverlaysForSlot(isOutputs, true, i == fluidCount - 1, false, false));
            tank.setId(FluidRecipeCapability.CAP.slotName(isOutputs ? IO.OUT : IO.IN, i));
            group.addWidget(tank);
            index++;
        }
        return group;
    }

    protected static int[] determineSlotsGrid(int itemInputsCount) {
        int itemSlotsToLeft;
        int itemSlotsToDown;
        double sqrt = Math.sqrt(itemInputsCount);
        //if the number of input has an integer root
        //return it.
        if (sqrt % 1 == 0) {
            itemSlotsToLeft = itemSlotsToDown = (int) sqrt;
        } else if (itemInputsCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            //if we couldn't fit all into a perfect square,
            //increase the amount of slots to the left
            itemSlotsToLeft = (int) Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            //if we still can't fit all the slots in a grid,
            //increase the amount of slots on the bottom
            if (itemInputsCount > itemSlotsToLeft * itemSlotsToDown) {
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

    public void appendJEIUI(GTRecipe recipe, WidgetGroup widgetGroup) {
        if (uiBuilder != null) {
            uiBuilder.accept(recipe, widgetGroup);
        }
    }

    public GTRecipe toGTrecipe(ResourceLocation id, Recipe<?> recipe) {
        var builder = recipeBuilder(id);
        for (var ingredient : recipe.getIngredients()) {
            builder.inputItems(ingredient);
        }
        builder.outputItems(recipe.getResultItem());
        if (recipe instanceof SmeltingRecipe smeltingRecipe) {
            builder.duration(smeltingRecipe.getCookingTime());
        }
        return GTRecipeSerializer.SERIALIZER.fromJson(id, builder.build().serializeRecipe());
    }
}
