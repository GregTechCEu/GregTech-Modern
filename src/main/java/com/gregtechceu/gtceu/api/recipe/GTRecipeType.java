package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.core.mixins.RecipeManagerInvoker;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    public GTRecipeType setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true, slotOverlay);
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
        for (GTRecipe recipe : recipeManager.getAllRecipesFor(this)) {
            if (recipe.isFuel && recipe.matchRecipe(holder).isSuccess() && recipe.matchTickRecipe(holder).isSuccess()) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    public List<GTRecipe> searchRecipe(RecipeManager recipeManager, IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies()) return Collections.emptyList();
        List<GTRecipe> matches = recipeManager.getAllRecipesFor(this).parallelStream()
                .filter(recipe -> !recipe.isFuel && recipe.matchRecipe(holder).isSuccess() && recipe.matchTickRecipe(holder).isSuccess())
                .collect(Collectors.toList());
        for (List<GTRecipe> recipes : proxyRecipes.values()) {
            var found = recipes.parallelStream()
                    .filter(recipe -> !recipe.isFuel && recipe.matchRecipe(holder).isSuccess() && recipe.matchTickRecipe(holder).isSuccess())
                    .toList();
            matches.addAll(found);
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
        return recipeBuilder(GTCEu.id(entry.tagPrefix + (entry.material == null ? "" : "_" + entry.material.getName())), append);
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

}
