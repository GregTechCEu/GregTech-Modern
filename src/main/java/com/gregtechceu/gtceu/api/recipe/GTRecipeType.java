package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeAdditionHandler;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

@Accessors(chain = true)
@SuppressWarnings("unused")
public class GTRecipeType implements RecipeType<GTRecipe> {

    public static final String LANGUAGE_KEY_PATH = "recipe_type";

    @Getter
    public GTRecipeSerializer serializer;

    @Getter
    public final ResourceLocation registryName;
    public final String group;

    public final Object2IntSortedMap<RecipeCapability<?>> maxInputs = new Object2IntAVLTreeMap<>(
            RecipeCapability.COMPARATOR);
    public final Object2IntSortedMap<RecipeCapability<?>> maxOutputs = new Object2IntAVLTreeMap<>(
            RecipeCapability.COMPARATOR);
    @Getter
    private final GTRecipeBuilder recipeBuilder;
    @Getter
    @Nullable
    private final Supplier<ItemStack> iconSupplier;
    @Nullable
    @Getter
    protected final Holder<SoundEntry> sound;
    @Getter
    protected List<Function<CompoundTag, String>> dataInfos = new ArrayList<>();
    @Getter
    protected boolean isScanner;
    // Does this recipe type have a research item slot? If this is true you MUST create a custom UI.
    @Getter
    protected final boolean hasResearchSlot;
    @Getter
    protected final Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> proxyRecipes;
    @Getter
    private final GTRecipeCategory category;
    @Getter
    private final Map<GTRecipeCategory, Set<GTRecipe>> categoryMap = new Object2ObjectOpenHashMap<>();
    private final RecipeDB db = new RecipeDB();
    @ApiStatus.Internal
    @Getter
    private final RecipeAdditionHandler additionHandler = new RecipeAdditionHandler(db);
    private final Map<String, Collection<GTRecipe>> researchEntries = new Object2ObjectOpenHashMap<>();
    @Getter
    private final List<ICustomRecipeLogic> customRecipeLogicRunners = new ArrayList<>();
    @Getter
    private final GTRecipeTypeUILayout uiLayout;

    public GTRecipeType(ResourceLocation id, Properties properties) {
        this.registryName = id;
        this.group = properties.group();
        this.category = GTRecipeCategory.registerDefault(this);
        this.serializer = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, registryName,
                new GTRecipeSerializer());

        this.maxInputs.putAll(properties.maxInputs());
        this.maxOutputs.putAll(properties.maxOutputs());
        this.iconSupplier = properties.iconSupplier();
        this.hasResearchSlot = properties.hasResearchSlot();
        this.isScanner = properties.isScanner();
        this.sound = properties.sound();
        this.recipeBuilder = new GTRecipeBuilder(id, this);
        if (properties.builderPreparer() != null) properties.builderPreparer().accept(recipeBuilder);
        if (properties.onRecipeBuild() != null) recipeBuilder.onSave(properties.onRecipeBuild());
        if (properties.uiLayout() != null) {
            this.uiLayout = properties.uiLayout().apply(new GTRecipeTypeUILayout.Builder(this)).build();
        } else {
            this.uiLayout = new GTRecipeTypeUILayout.Builder(this).build();
        }
        // must be linked to stop json contents from shuffling
        Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (RecipeType<?> proxyRecipe : properties.proxyRecipes()) {
            map.put(proxyRecipe, new ArrayList<>());
        }
        this.proxyRecipes = map;
        category.setXEIVisible(properties.recipeViewerCategoryVisible());
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    public Iterator<GTRecipe> searchRecipe(IRecipeCapabilityHolder holder, Predicate<GTRecipe> canHandle) {
        if (!holder.hasCapabilityProxies()) return Collections.emptyIterator();
        var iterator = db.iterator(holder, canHandle);
        if (iterator == null) {
            return Collections.emptyIterator();
        }
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

    public int getMaxSlots(RecipeCapability<?> cap, IO io) {
        return io == IO.IN ? getMaxInputs(cap) : getMaxOutputs(cap);
    }

    //////////////////////////////////////
    // ***** Recipe Builder ******//
    //////////////////////////////////////

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

    @ApiStatus.Internal
    public GTRecipeBuilder recipeBuilder(String id) {
        return recipeBuilder(GTCEu.id(id));
    }

    @ApiStatus.Internal
    public GTRecipeBuilder recipeBuilder(String id, Object... append) {
        return recipeBuilder(GTCEu.id(id), append);
    }

    public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return recipeBuilder.copyFrom(builder);
    }

    public void addDataStickEntry(String researchId, GTRecipe recipe) {
        Collection<GTRecipe> collection = researchEntries.computeIfAbsent(researchId, (k) -> new ObjectOpenHashSet<>());
        collection.add(recipe);
    }

    @Nullable
    public Collection<GTRecipe> getDataStickEntry(String researchId) {
        return researchEntries.get(researchId);
    }

    public boolean removeDataStickEntry(String researchId, GTRecipe recipe) {
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

    public RecipeHolder<GTRecipe> toGTRecipe(RecipeHolder<?> holder) {
        var builder = recipeBuilder(holder.id());
        Recipe<?> recipe = holder.value();
        for (var ingredient : recipe.getIngredients()) {
            builder.inputItems(new SizedIngredient(ingredient, 1));
        }
        builder.outputItems(recipe.getResultItem(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)));
        if (recipe instanceof SmeltingRecipe smeltingRecipe) {
            builder.duration(smeltingRecipe.getCookingTime());
        }
        GTRecipe built = builder.build();
        return new RecipeHolder<>(built.id, built);
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

    public String getTranslationKey() {
        return this.registryName.toLanguageKey(LANGUAGE_KEY_PATH);
    }

    public Component getName() {
        return Component.translatable(getTranslationKey());
    }

    public RecipeDB db() {
        return db;
    }

    @ApiStatus.Internal
    public void beginStagingRecipes() {
        categoryMap.clear();
        additionHandler.beginStaging();
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

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static class Properties {

        public String group;
        public final RecipeType<?>[] proxyRecipes;

        private final Object2IntSortedMap<RecipeCapability<?>> maxInputs = new Object2IntAVLTreeMap<>(
                RecipeCapability.COMPARATOR);
        private final Object2IntSortedMap<RecipeCapability<?>> maxOutputs = new Object2IntAVLTreeMap<>(
                RecipeCapability.COMPARATOR);
        private final List<ICustomRecipeLogic> customRecipeLogicRunners = new ArrayList<>();

        @Nullable
        private Supplier<ItemStack> iconSupplier;
        @Nullable
        protected Holder<SoundEntry> sound = null;
        protected boolean isScanner = false;
        protected boolean hasResearchSlot = false;
        private boolean recipeViewerCategoryVisible = true;
        private @Nullable Function<GTRecipeTypeUILayout.Builder, GTRecipeTypeUILayout.Builder> uiLayout = null;
        private @Nullable Consumer<GTRecipeBuilder> builderPreparer = null;
        private @Nullable BiConsumer<GTRecipeBuilder, RecipeOutput> onRecipeBuild = null;

        public Properties(String group, RecipeType<?>... proxyRecipes) {
            this.group = group;
            this.proxyRecipes = proxyRecipes;
        }
    }
}
