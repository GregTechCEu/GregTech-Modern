package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.material.material.stack.UnificationEntry;
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
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
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

    public static final String LANGUAGE_KEY_PATH = "recipe_type";
    public static final List<ICustomScannerLogic> CUSTOM_SCANNER_LOGICS = new ArrayList<>();

    @Getter
    @Setter(onMethod_ = { @ApiStatus.Internal })
    public GTRecipeSerializer serializer;

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
    protected final Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> proxyRecipes;
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
        Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> map = new Object2ObjectLinkedOpenHashMap<>();
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
    public Iterator<GTRecipe> searchFuelRecipe(IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies() || !isFuelRecipeType()) return null;
        return getLookup().getRecipeIterator(holder, recipe -> recipe.isFuel &&
                recipe.matchRecipe(holder).isSuccess() && recipe.matchTickRecipe(holder).isSuccess());
    }

    public Iterator<GTRecipe> searchRecipe(IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies()) return null;
        var iterator = getLookup().getRecipeIterator(holder, recipe -> !recipe.isFuel &&
                recipe.matchRecipe(holder).isSuccess() && recipe.matchTickRecipe(holder).isSuccess());
        boolean any = false;
        while (iterator.hasNext()) {
            GTRecipe recipe = iterator.next();
            if (recipe == null) continue;
            any = true;
            break;
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

    public RecipeHolder<GTRecipe> toGTrecipe(RecipeHolder<?> recipe) {
        var builder = recipeBuilder(recipe.id());
        for (var ingredient : recipe.value().getIngredients()) {
            builder.inputItems(new SizedIngredient(ingredient, 1));
        }
        builder.outputItems(
                recipe.value().getResultItem(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)));
        if (recipe.value() instanceof SmeltingRecipe smeltingRecipe) {
            builder.duration(smeltingRecipe.getCookingTime());
        }
        return new RecipeHolder<>(builder.id, builder.build());
    }

    public @NotNull List<RecipeHolder<GTRecipe>> getRepresentativeRecipes() {
        List<RecipeHolder<GTRecipe>> recipes = new ArrayList<>();
        for (ICustomScannerLogic logic : CUSTOM_SCANNER_LOGICS) {
            List<RecipeHolder<GTRecipe>> logicRecipes = logic.getRepresentativeRecipes();
            if (logicRecipes != null && !logicRecipes.isEmpty()) {
                recipes.addAll(logicRecipes);
            }
        }
        return recipes;
    }

    public String getTranslationKey() {
        return this.registryName.toLanguageKey(LANGUAGE_KEY_PATH);
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
        default List<RecipeHolder<GTRecipe>> getRepresentativeRecipes() {
            return null;
        }
    }
}
