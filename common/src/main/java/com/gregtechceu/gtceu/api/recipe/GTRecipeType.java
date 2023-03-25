package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.core.mixins.RecipeManagerInvoker;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
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
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

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
    private final Object2IntMap<RecipeCapability<?>> maxInputs = new Object2IntOpenHashMap<>();
    private final Object2IntMap<RecipeCapability<?>> maxOutputs = new Object2IntOpenHashMap<>();
    private final Object2IntMap<RecipeCapability<?>> minInputs = new Object2IntOpenHashMap<>();
    private final Object2IntMap<RecipeCapability<?>> minOutputs = new Object2IntOpenHashMap<>();
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

    public GTRecipeType(ResourceLocation registryName, RecipeType<?>... proxyRecipes) {
        this.registryName = registryName;
        recipeBuilder = new GTRecipeBuilder(registryName, this);
        // must be linked to stop json contents from shuffling
        Map<RecipeType<?>, List<GTRecipe>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (RecipeType<?> proxyRecipe : proxyRecipes) {
            map.put(proxyRecipe, new ArrayList<>());
        }
        this.proxyRecipes = map;
    }

    public GTRecipeType setIOSize(int minInputs, int maxInputs, int minOutputs, int maxOutputs, int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs) {
        return setMinSize(IO.IN, ItemRecipeCapability.CAP, minInputs).setMinSize(IO.IN, FluidRecipeCapability.CAP, minFluidInputs)
                .setMinSize(IO.OUT, ItemRecipeCapability.CAP, minOutputs).setMinSize(IO.OUT, FluidRecipeCapability.CAP, minFluidOutputs)
                .setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxInputs).setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
                .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxOutputs).setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
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

    public GTRecipeType setMinSize(IO io, RecipeCapability<?> cap, int max) {
        if (io == IO.IN || io == IO.BOTH) {
            minInputs.put(cap, max);
        }
        if (io == IO.OUT || io == IO.BOTH) {
            minOutputs.put(cap, max);
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

    public int getMinInputs(RecipeCapability<?> cap) {
        return minInputs.getOrDefault(cap, 0);

    }

    public int getMinOutputs(RecipeCapability<?> cap) {
        return minOutputs.getOrDefault(cap, 0);
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

    public Size getJEISize() {
        return new Size(176, 120 + dataInfos.size() * 10);
    }

    /**
     * Auto layout UI template for recipes.
     * @param progressSupplier progress. To create a JEI / REI UI, use the para {@link ProgressWidget#JEIProgress}.
     */
    public WidgetGroup createUITemplate(DoubleSupplier progressSupplier, IItemTransfer importItems, IItemTransfer exportItems, IFluidStorage[] importFluids, IFluidStorage[] exportFluids, boolean isSteam, boolean isHighPressure) {
        var group = new WidgetGroup(0, 0, 176, 83);
        group.addWidget(new ProgressWidget(progressSupplier, 78, 23, 20, 20,
                (isSteam && steamProgressBarTexture != null) ? new ProgressTexture(
                        steamProgressBarTexture.get(isHighPressure).getSubTexture(0, 0, 1, 0.5),
                        steamProgressBarTexture.get(isHighPressure).getSubTexture(0, 0.5, 1, 0.5))
                        .setFillDirection(steamMoveType)
                        : progressBarTexture));
        addInventorySlotGroup(group, importItems, importFluids, false, progressSupplier == ProgressWidget.JEIProgress, isSteam, isHighPressure);
        addInventorySlotGroup(group, exportItems, exportFluids, true, progressSupplier == ProgressWidget.JEIProgress, isSteam, isHighPressure);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(group);
        return group;
    }

    public WidgetGroup createUITemplate(DoubleSupplier progressSupplier, IItemTransfer importItems, IItemTransfer exportItems, IFluidStorage[] importFluids, IFluidStorage[] exportFluids) {
        return createUITemplate(progressSupplier, importItems, exportItems, importFluids, exportFluids, false, false);
    }

    protected void addSpecialTexture(WidgetGroup group) {
        group.addWidget(new ImageWidget(specialTexturePosition.left, specialTexturePosition.up, specialTexturePosition.getWidth(), specialTexturePosition.getHeight(), specialTexture));
    }

    protected void addInventorySlotGroup(WidgetGroup group, IItemTransfer itemHandler, IFluidStorage[] fluidHandler, boolean isOutputs, boolean isJEI, boolean isSteam, boolean isHighPressure) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.length;
        boolean invertFluids = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            itemInputsCount = fluidInputsCount;
            fluidInputsCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 70 - itemSlotsToLeft * 18;
        int startInputsY = 33 - (int) (itemSlotsToDown / 2.0 * 18);
        boolean wasGroup = itemHandler.getSlots() + fluidHandler.length == 12;
        if (wasGroup) startInputsY -= 9;
        else if (itemHandler.getSlots() >= 6 && fluidHandler.length >= 2 && !isOutputs) startInputsY -= 9;
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                if (slotIndex >= itemInputsCount) break;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                addSlot(group, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs, isJEI, isSteam, isHighPressure);
            }
        }
        if (wasGroup) startInputsY += 2;
        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int y = startInputsY + 18 * i;
                    addSlot(group, startSpecX, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs, isJEI, isSteam, isHighPressure);
                }
            } else {
                int startSpecY = startInputsY + itemSlotsToDown * 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(group, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs, isJEI, isSteam, isHighPressure);
                }
            }
        }
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

    protected void addSlot(WidgetGroup group, int x, int y, int slotIndex, IItemTransfer itemHandler, IFluidStorage[] fluidHandler, boolean isFluid, boolean isOutputs, boolean isJEI, boolean isSteam, boolean isHighPressure) {
        if (!isFluid) {
            group.addWidget(new SlotWidget(itemHandler, slotIndex, x, y, !isJEI, !isJEI && !isOutputs)
                    .setIngredientIO(isOutputs ? IngredientIO.OUTPUT : IngredientIO.INPUT)
                    .setBackground(getOverlaysForSlot(isOutputs, false, slotIndex == itemHandler.getSlots() - 1, isSteam, isHighPressure))
            );
        } else {
            group.addWidget(new TankWidget(fluidHandler[slotIndex], x, y, 18, 18, !isJEI, !isJEI && !isOutputs)
                    .setIngredientIO(isOutputs ? IngredientIO.OUTPUT : IngredientIO.INPUT)
                    .setBackground(getOverlaysForSlot(isOutputs, true, slotIndex == fluidHandler.length - 1, isSteam, isHighPressure))
            );
        }
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
