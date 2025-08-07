package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookup;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

@Accessors(chain = true)
public class GTRecipeType implements RecipeType<GTRecipe> {

    public final ResourceLocation registryName;
    public final String group;
    public final Object2IntSortedMap<RecipeCapability<?>> maxInputs = new Object2IntAVLTreeMap<>(
            RecipeCapability.COMPARATOR);
    public final Object2IntSortedMap<RecipeCapability<?>> maxOutputs = new Object2IntAVLTreeMap<>(
            RecipeCapability.COMPARATOR);
    @Setter
    private GTRecipeBuilder recipeBuilder;
    @Getter
    @Setter
    private ChanceBoostFunction chanceFunction = ChanceBoostFunction.NONE;
    @Getter
    @Setter
    private GTRecipeTypeUI recipeUI = new GTRecipeTypeUI(this);
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
    @Getter
    @Setter
    protected boolean isScanner;
    // Does this recipe type have a research item slot? If this is true you MUST create a custom UI.
    @Getter
    @Setter
    protected boolean hasResearchSlot;
    @Getter
    protected final Map<RecipeType<?>, List<GTRecipe>> proxyRecipes;
    @Getter
    private final GTRecipeCategory category;
    @Getter
    private final Map<GTRecipeCategory, Set<GTRecipe>> categoryMap = new Object2ObjectOpenHashMap<>();
    @Getter
    private final GTRecipeLookup lookup = new GTRecipeLookup(this);
    @Setter
    @Getter
    private boolean offsetVoltageText = false;
    @Setter
    @Getter
    private int voltageTextOffset = 20;
    private final Map<String, Collection<GTRecipe>> researchEntries = new Object2ObjectOpenHashMap<>();
    @Getter
    private final List<ICustomRecipeLogic> customRecipeLogicRunners = new ArrayList<>();
    @Getter
    private int minRecipeConditions = 0;

    public GTRecipeType(ResourceLocation registryName, String group, RecipeType<?>... proxyRecipes) {
        this.registryName = registryName;
        this.group = group;
        this.category = GTRecipeCategory.registerDefault(this);
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

    public GTRecipeType setMaxTooltips(int maxTooltips) {
        this.recipeUI.setMaxTooltips(maxTooltips);
        return this;
    }

    public GTRecipeType setXEIVisible(boolean XEIVisible) {
        this.category.setXEIVisible(XEIVisible);
        return this;
    }

    public GTRecipeType addDataInfo(Function<CompoundTag, String> dataInfo) {
        this.dataInfos.add(dataInfo);
        return this;
    }

    public void setMinRecipeConditions(int n) {
        minRecipeConditions = Math.max(minRecipeConditions, n);
    }

    /**
     *
     * @param recipeLogic A function which is passed the normal findRecipe() result. Returns null if no valid recipe for
     *                    the custom logic is found.
     */
    public GTRecipeType addCustomRecipeLogic(ICustomRecipeLogic recipeLogic) {
        this.customRecipeLogicRunners.add(recipeLogic);
        return this;
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    public @NotNull Iterator<GTRecipe> searchRecipe(IRecipeCapabilityHolder holder, Predicate<GTRecipe> canHandle) {
        if (!holder.hasCapabilityProxies()) return Collections.emptyIterator();
        var iterator = getLookup().getRecipeIterator(holder, canHandle);
        boolean any = false;
        while (iterator.hasNext()) {
            GTRecipe recipe = iterator.next();
            if (recipe == null) continue;
            any = true;
            break;
        }

        if (any) {
            iterator.reset();
            return iterator;
        }

        for (ICustomRecipeLogic logic : customRecipeLogicRunners) {
            GTRecipe recipe = logic.createCustomRecipe(holder);
            if (recipe != null && canHandle.test(recipe)) return Collections.singleton(recipe).iterator();
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

    public GTRecipeBuilder recipeBuilder(ResourceLocation id) {
        return recipeBuilder.copy(id);
    }

    public GTRecipeBuilder recipeBuilder(ResourceLocation id, Object... append) {
        if (append.length > 0) {
            String toAppend = Arrays.stream(append)
                    .map(Object::toString)
                    .map(FormattingUtil::toLowerCaseUnderscore)
                    .reduce("", (a, b) -> a + "_" + b);
            id = id.withSuffix(toAppend);
        }
        return recipeBuilder(id);
    }

    public GTRecipeBuilder recipeBuilder(String id) {
        return recipeBuilder(GTCEu.id(id));
    }

    public GTRecipeBuilder recipeBuilder(String id, Object... append) {
        return recipeBuilder(GTCEu.id(id), append);
    }

    public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return recipeBuilder.copyFrom(builder);
    }

    public GTRecipeType onRecipeBuild(BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onBuild) {
        recipeBuilder.onSave(onBuild);
        return this;
    }

    public void addDataStickEntry(@NotNull String researchId, @NotNull GTRecipe recipe) {
        Collection<GTRecipe> collection = researchEntries.computeIfAbsent(researchId, (k) -> new ObjectOpenHashSet<>());
        collection.add(recipe);
    }

    @Nullable
    public Collection<GTRecipe> getDataStickEntry(@NotNull String researchId) {
        return researchEntries.get(researchId);
    }

    public boolean removeDataStickEntry(@NotNull String researchId, @NotNull GTRecipe recipe) {
        Collection<GTRecipe> collection = researchEntries.get(researchId);
        if (collection == null) return false;
        if (collection.remove(recipe)) {
            if (collection.isEmpty()) {
                return researchEntries.remove(researchId) != null;
            }
            return true;
        }
        return false;
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

    public void buildRepresentativeRecipes() {
        for (ICustomRecipeLogic logic : customRecipeLogicRunners) {
            logic.buildRepresentativeRecipes();
        }
    }

    public void addToMainCategory(GTRecipe recipe) {
        addToCategoryMap(category, recipe);
    }

    public void addToCategoryMap(GTRecipeCategory category, GTRecipe recipe) {
        categoryMap.computeIfAbsent(category, k -> new ObjectLinkedOpenHashSet<>()).add(recipe);
    }

    public Set<GTRecipeCategory> getCategories() {
        return Collections.unmodifiableSet(categoryMap.keySet());
    }

    public Set<GTRecipe> getRecipesInCategory(GTRecipeCategory category) {
        return Collections.unmodifiableSet(categoryMap.getOrDefault(category, Set.of()));
    }

    public interface ICustomRecipeLogic {

        /**
         * @return A custom recipe to run given the current holder's inputs. Will be called only if a registered
         *         recipe is not found to run. Return null if no recipe should be run by your logic.
         */
        @Nullable
        GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder);

        /**
         * Build all representative recipes in this method, then add them to the appropriate recipe category.
         * These are added to XEI to demonstrate the custom logic.
         * Not required, can NOOP if unneeded.
         */
        default void buildRepresentativeRecipes() {}
    }
}
