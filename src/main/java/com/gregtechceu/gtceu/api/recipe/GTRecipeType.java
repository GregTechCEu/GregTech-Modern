package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.core.mixins.RecipeManagerInvoker;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.JEIPlugin;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Rect;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.emi.emi.api.EmiApi;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipeType
 */
@Accessors(chain = true)
public class GTRecipeType implements RecipeType<GTRecipe> {

    public static final List<ICustomScannerLogic> CUSTOM_SCANNER_LOGICS = new ArrayList<>();

    public final ResourceLocation registryName;
    public final String group;
    public final TreeMap<RecipeCapability<?>, Integer> maxInputs = new TreeMap<>(RecipeCapability.COMPARATOR);
    public final TreeMap<RecipeCapability<?>, Integer> maxOutputs = new TreeMap<>(RecipeCapability.COMPARATOR);
    @Setter
    private GTRecipeBuilder recipeBuilder;
    @Getter
    @Setter
    private GTRecipeTypeUI recipeUI = new GTRecipeTypeUI(this);
    @Getter
    private final Byte2ObjectMap<IGuiTexture> slotOverlays = new Byte2ObjectArrayMap<>();
    @Setter
    @Getter
    private GTRecipeType smallRecipeMap;
    @Setter
    @Getter
    @Nullable
    private Supplier<ItemStack> iconSupplier;
    @Nullable
    @Setter
    @Getter
    protected SoundEntry sound;
    @Getter
    protected List<Function<CompoundTag, String>> dataInfos = new ArrayList<>();
    @Setter
    @Getter
    protected int maxTooltips = 3;
    @Setter
    @Getter
    protected boolean isFuelRecipeType;
    @Getter
    @Setter
    protected boolean isScanner;
    // Does this recipe type have a research item slot? If this is true you MUST create a custom UI.
    @Getter
    @Setter
    protected boolean hasResearchSlot;
    @Getter
    protected final Map<RecipeType<?>, List<GTRecipe>> proxyRecipes;
    private CompoundTag customUICache;
    @Getter
    private final GTRecipeLookup lookup = new GTRecipeLookup(this);
    @Setter
    @Getter
    private boolean offsetVoltageText = false;
    @Setter
    @Getter
    private int voltageTextOffset = 20;
    private final Map<String, Collection<GTRecipe>> researchEntries = new Object2ObjectOpenHashMap<>();

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
        return setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxInputs)
                .setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
                .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxOutputs)
                .setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
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

    public GTRecipeType setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        this.recipeUI.setSlotOverlay(isOutput, isFluid, slotOverlay);
        return this;
    }

    public GTRecipeType setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, IGuiTexture slotOverlay) {
        this.recipeUI.setSlotOverlay(isOutput, isFluid, isLast, slotOverlay);
        return this;
    }

    public GTRecipeType setProgressBar(ResourceTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.recipeUI.setProgressBar(progressBar, moveType);
        return this;
    }

    public GTRecipeType setSteamProgressBar(SteamTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.recipeUI.setSteamProgressBarTexture(progressBar);
        this.recipeUI.setSteamMoveType(moveType);
        return this;
    }

    public GTRecipeType setUiBuilder(BiConsumer<GTRecipe, WidgetGroup> uiBuilder) {
        this.recipeUI.setUiBuilder(uiBuilder);
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
        for (net.minecraft.world.item.crafting.RecipeHolder<GTRecipe> recipe : recipeManager.getAllRecipesFor(this)) {
            if (recipe.value().isFuel && recipe.value().matchRecipe(holder).isSuccess() && recipe.value().matchTickRecipe(holder).isSuccess()) {
                matches.add(recipe.value());
            }
        }
        return matches;
    }

    public List<GTRecipe> searchRecipe(RecipeManager recipeManager, IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies()) return Collections.emptyList();
        List<GTRecipe> matches = recipeManager.getAllRecipesFor(this).parallelStream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .filter(recipe -> !recipe.isFuel && recipe.matchRecipe(holder).isSuccess() && recipe.matchTickRecipe(holder).isSuccess())
                .collect(Collectors.toList());
        for (List<GTRecipe> recipes : proxyRecipes.values()) {
            var found = recipes.parallelStream()
                    .filter(recipe -> !recipe.isFuel && recipe.matchRecipe(holder).isSuccess() && recipe.matchTickRecipe(holder).isSuccess())
                    .toList();
            matches.addAll(found);
        }
        if (!this.isScanner || any) {
            iterator.reset();
            return iterator;
        }

        for (ICustomScannerLogic logic : CUSTOM_SCANNER_LOGICS) {
            GTRecipe recipe = logic.createCustomRecipe(holder);
            if (recipe != null) return Collections.singleton(recipe).iterator();
        }
        return Collections.emptyIterator();
    }

    public int getMaxInputs(RecipeCapability<?> cap) {
        return maxInputs.getOrDefault(cap, 0);
    }

    public int getMaxOutputs(RecipeCapability<?> cap) {
        return maxOutputs.getOrDefault(cap, 0);
    }

    //////////////////////////////////////
    // ***** Recipe Builder ******//
    //////////////////////////////////////

    public GTRecipeType prepareBuilder(Consumer<GTRecipeBuilder> onPrepare) {
        onPrepare.accept(recipeBuilder);
        return this;
    }

    public GTRecipeBuilder recipeBuilder(ResourceLocation id, Object... append) {
        if (append.length > 0) {
            return recipeBuilder.copy(new ResourceLocation(id.getNamespace(),
                    id.getPath() + Arrays.stream(append).map(Object::toString).map(FormattingUtil::toLowerCaseUnder)
                            .reduce("", (a, b) -> a + "_" + b)));
        }
        return recipeBuilder.copy(id);
    }

    public GTRecipeBuilder recipeBuilder(String id, Object... append) {
        return recipeBuilder(GTCEu.id(id), append);
    }

    public GTRecipeBuilder recipeBuilder(UnificationEntry entry, Object... append) {
        return recipeBuilder(GTCEu.id(entry.tagPrefix + (entry.material == null ? "" : "_" + entry.material.getName())),
                append);
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

    public GTRecipeType onRecipeBuild(BiConsumer<GTRecipeBuilder, RecipeOutput> onBuild) {
        recipeBuilder.onSave(onBuild);
        return this;
    }

    public void addDataStickEntry(@NotNull String researchId, @NotNull GTRecipe recipe) {
        Collection<GTRecipe> collection = researchEntries.computeIfAbsent(researchId, (k) -> new ObjectOpenHashSet<>());
        collection.add(recipe);
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
                    var resource = resourceManager.getResourceOrThrow(new ResourceLocation(registryName.getNamespace(), "ui/recipe_type/%s.rtui".formatted(registryName.getPath())));
                    try (InputStream inputStream = resource.open()){
                        try (DataInputStream dataInputStream = new DataInputStream(inputStream);){
                            this.customUICache = NbtIo.read(dataInputStream, NbtAccounter.unlimitedHeap());
                        }
                    }
                } catch (Exception e) {
                    this.customUICache = new CompoundTag();
                }
                if (this.customUICache == null) {
                    this.customUICache = new CompoundTag();
                }
            }
            return true;
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
        return new Size(176, (dataInfos.size() + maxTooltips) * 10 + 5 + createEditableUITemplate(false, false).createDefault().getSize().height);
    }

    public record RecipeHolder(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, IFluidHandler importFluids, IFluidHandler exportFluids, boolean isSteam, boolean isHighPressure) {};

    /**
     * Auto layout UI template for recipes.
     * @param progressSupplier progress. To create a JEI / REI UI, use the para {@link ProgressWidget#JEIProgress}.
     */
    public WidgetGroup createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, IFluidHandler importFluids, IFluidHandler exportFluids, boolean isSteam, boolean isHighPressure) {
        var template = createEditableUITemplate(isSteam, isHighPressure);
        var group = template.createDefault();
        template.setupUI(group, new RecipeHolder(progressSupplier, importItems, exportItems, importFluids, exportFluids, isSteam, isHighPressure));
        return group;
    }

    public WidgetGroup createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, IFluidHandler importFluids, IFluidHandler exportFluids) {
        return createUITemplate(progressSupplier, importItems, exportItems, importFluids, exportFluids, false, false);
    }

    /**
     * Auto layout UI template for recipes.
     */
    public IEditableUI<WidgetGroup, RecipeHolder> createEditableUITemplate(boolean isSteam, boolean isHighPressure) {
        return new IEditableUI.Normal<>(() -> {
            var isCustomUI = !isSteam && hasCustomUI();
            if (isCustomUI) {
                var nbt = getCustomUI();
                var group = new WidgetGroup();
                IConfigurableWidget.deserializeNBT(group, nbt.getCompound("root"), Resources.fromNBT(nbt.getCompound("resources")), false);
                group.setSelfPosition(new Position(0, 0));
                return group;
            }

            var inputs = addInventorySlotGroup(false, isSteam, isHighPressure);
            var outputs = addInventorySlotGroup(true, isSteam, isHighPressure);
            var group = new WidgetGroup(0, 0, inputs.getSize().width + outputs.getSize().width + 40, Math.max(inputs.getSize().height, outputs.getSize().height));
            var size = group.getSize();

            inputs.addSelfPosition(0, (size.height - inputs.getSize().height) / 2);
            outputs.addSelfPosition(inputs.getSize().width + 40, (size.height - outputs.getSize().height) / 2);
            group.addWidget(inputs);
            group.addWidget(outputs);

            var progressWidget = new ProgressWidget(ProgressWidget.JEIProgress, inputs.getSize().width + 10, size.height / 2 - 10, 20, 20, progressBarTexture);
            progressWidget.setId("progress");
            group.addWidget(progressWidget);

            progressWidget.setProgressTexture((isSteam && steamProgressBarTexture != null) ? new ProgressTexture(
                    steamProgressBarTexture.get(isHighPressure).getSubTexture(0, 0, 1, 0.5),
                    steamProgressBarTexture.get(isHighPressure).getSubTexture(0, 0.5, 1, 0.5))
                    .setFillDirection(steamMoveType)
                    : progressBarTexture);

            if (specialTexture != null && specialTexturePosition != null) addSpecialTexture(group);
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
                                ViewSearchBuilder.builder().addCategory(GTRecipeTypeDisplayCategory.CATEGORIES.apply(GTRecipeType.this)).open();
                            } else if (LDLib.isJeiLoaded()) {
                                JEIPlugin.jeiRuntime.getRecipesGui().showTypes(List.of(GTRecipeTypeCategory.TYPES.apply(GTRecipeType.this)));
                            } else if (LDLib.isEmiLoaded()) {
                                EmiApi.displayRecipeCategory(GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeType.this));
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

    protected void addSpecialTexture(WidgetGroup group) {
        group.addWidget(new ImageWidget(specialTexturePosition.left, specialTexturePosition.up, specialTexturePosition.getWidth(), specialTexturePosition.getHeight(), specialTexture));
    }

    protected WidgetGroup addInventorySlotGroup(boolean isOutputs, boolean isSteam, boolean isHighPressure) {

        var itemCount = isOutputs ? getMaxOutputs(ItemRecipeCapability.CAP) : getMaxInputs(ItemRecipeCapability.CAP);
        var fluidCount = isOutputs ? getMaxOutputs(FluidRecipeCapability.CAP) : getMaxInputs(FluidRecipeCapability.CAP);
        var sum = itemCount + fluidCount;
        WidgetGroup group = new WidgetGroup(0, 0, Math.min(sum, 3) * 18 + 8, ((sum + 2) / 3) * 18 + 8);
        int index = 0;
        for (int slotIndex = 0; slotIndex < itemCount; slotIndex++) {
            var slot = new SlotWidget();
            slot.initTemplate();
            slot.setSelfPosition(new Position((index % 3) * 18 + 4, (index / 3) * 18 + 4));
            slot.setBackground(getOverlaysForSlot(isOutputs, false, slotIndex == itemCount - 1, isSteam, isHighPressure));
            slot.setId(ItemRecipeCapability.CAP.slotName(isOutputs ? IO.OUT : IO.IN, slotIndex));
            group.addWidget(slot);
            index++;
        }
        for (int i = 0; i < fluidCount; i++) {
            var tank = new TankWidget();
            tank.initTemplate();
            tank.setFillDirection(ProgressTexture.FillDirection.ALWAYS_FULL);
            tank.setSelfPosition(new Position((index % 3) * 18 + 4, (index / 3) * 18 + 4));
            tank.setBackground(getOverlaysForSlot(isOutputs, true, i == fluidCount - 1, isSteam, isHighPressure));
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
        builder.outputItems(recipe.getResultItem(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)));
        if (recipe instanceof SmeltingRecipe smeltingRecipe) {
            builder.duration(smeltingRecipe.getCookingTime());
        }
        return GTRecipeSerializer.SERIALIZER.fromJson(id, builder.build().serializeRecipe());
    }

    public @NotNull List<GTRecipe> getRepresentativeRecipes() {
        List<GTRecipe> recipes = new ArrayList<>();
        for (ICustomScannerLogic logic : CUSTOM_SCANNER_LOGICS) {
            List<GTRecipe> logicRecipes = logic.getRepresentativeRecipes();
            if (logicRecipes != null && !logicRecipes.isEmpty()) {
                recipes.addAll(logicRecipes);
            }
        }
        return recipes;
    }

    /**
     *
     * @param logic A function which is passed the normal findRecipe() result. Returns null if no valid recipe for the
     *              custom logic is found.
     */
    public static void registerCustomScannerLogic(ICustomScannerLogic logic) {
        CUSTOM_SCANNER_LOGICS.add(logic);
    }

    public interface ICustomScannerLogic {

        /**
         * @return A custom recipe to run given the current Scanner's inputs. Will be called only if a registered
         *         recipe is not found to run. Return null if no recipe should be run by your logic.
         */
        @Nullable
        GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder);

        /**
         * @return A list of Recipes that are never registered, but are added to JEI to demonstrate the custom logic.
         *         Not required, can return empty or null to not add any.
         */
        @Nullable
        default List<GTRecipe> getRepresentativeRecipes() {
            return null;
        }
    }
}
