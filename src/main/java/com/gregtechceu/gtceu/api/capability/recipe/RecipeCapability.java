package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.codec.DispatchedMapCodec;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Used to detect whether a machine has a certain capability.
 */
public abstract class RecipeCapability<T> {

    public static final Codec<RecipeCapability<?>> DIRECT_CODEC = GTRegistries.RECIPE_CAPABILITIES.codec();
    public static final Codec<Map<RecipeCapability<?>, List<Content>>> CODEC = new DispatchedMapCodec<>(
            RecipeCapability.DIRECT_CODEC,
            RecipeCapability::contentCodec);
    public static final Comparator<RecipeCapability<?>> COMPARATOR = Comparator.comparingInt(o -> o.sortIndex);

    public final String name;
    public final int color;
    public final boolean doRenderSlot;
    public final int sortIndex;
    public final IContentSerializer<T> serializer;

    protected RecipeCapability(String name, int color, boolean doRenderSlot, int sortIndex,
                               IContentSerializer<T> serializer) {
        this.name = name;
        this.color = color;
        this.doRenderSlot = doRenderSlot;
        this.sortIndex = sortIndex;
        this.serializer = serializer;
    }

    public static Codec<List<Content>> contentCodec(RecipeCapability<?> capability) {
        return Content.codec(capability).listOf();
    }

    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    public T copyInner(T content) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        serializer.toNetwork(buf, content);
        return serializer.fromNetwork(buf);
    }

    /**
     * deep copy and modify the size attribute for those Content that have the size attribute.
     */
    public T copyWithModifier(T content, ContentModifier modifier) {
        return copyInner(content);
    }

    @SuppressWarnings("unchecked")
    public final T copyContent(Object content) {
        return copyInner((T) content);
    }

    @SuppressWarnings("unchecked")
    public final T copyContent(Object content, ContentModifier modifier) {
        return copyWithModifier((T) content, modifier);
    }

    /**
     * used for recipe builder via KubeJs.
     */
    public T of(Object o) {
        return serializer.of(o);
    }

    public String slotName(IO io) {
        return "%s_%s".formatted(name, io.name().toLowerCase(Locale.ROOT));
    }

    public String slotName(IO io, int index) {
        return "%s_%s_%s".formatted(name, io.name().toLowerCase(Locale.ROOT), index);
    }

    public Component getName() {
        return Component.translatable("recipe.capability.%s.name".formatted(name));
    }

    public boolean isRecipeSearchFilter() {
        return false;
    }

    /**
     * Convert the passed object to a list of recipe lookup filters.
     *
     * @param ingredient ingredient. e.g. for ITEM, this can be Ingredient or ItemStack
     * @return a list of recipe lookup filters.
     */
    public List<AbstractMapIngredient> convertToMapIngredient(Object ingredient) {
        return List.of();
    }

    public List<Object> compressIngredients(Collection<Object> ingredients) {
        return new ArrayList<>(ingredients);
    }

    /**
     * Does the recipe test if this capability is workable? if not, you should test validity somewhere else.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean doMatchInRecipe() {
        return true;
    }

    /**
     * maximum parallel amount based on the inputs (and possibly outputs) provided.
     *
     * @param recipe     the recipe from which we get the input to product ratio
     * @param holder     the {@link IRecipeCapabilityHolder} that contains all the inputs and outputs of the machine.
     * @param multiplier the maximum possible multiplied we can get from the input inventory
     *                   see {@link ParallelLogic#getMaxRecipeMultiplier(GTRecipe, IRecipeCapabilityHolder, int)}
     * @return the amount of times a {@link GTRecipe} outputs can be merged into an inventory without voiding products.
     */
    // returns Integer.MAX_VALUE by default, to skip processing.
    public int limitParallel(GTRecipe recipe, IRecipeCapabilityHolder holder, int multiplier) {
        return Integer.MAX_VALUE;
    }

    /**
     * Finds the maximum number of GTRecipes that can be performed at the same time based on the contents of input
     * inventories
     *
     * @param holder         The {@link IRecipeCapabilityHolder} that contains all the inputs and outputs of the
     *                       machine.
     * @param recipe         The {@link GTRecipe} for which to find the maximum that can be run simultaneously
     * @param parallelAmount The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of GTRecipes that can be performed at a single time based on the available Items
     */
    // returns Integer.MAX_VALUE by default, to skip processing.
    public int getMaxParallelRatio(IRecipeCapabilityHolder holder, GTRecipe recipe, int parallelAmount) {
        return Integer.MAX_VALUE;
    }

    public boolean doAddGuiSlots() {
        return isRecipeSearchFilter();
    }

    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {}

    @NotNull
    public List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        return new ArrayList<>();
    }

    @Nullable
    public Object createXEIContainer(List<?> contents) {
        return null;
    }

    @Nullable("null when getWidgetClass() == null")
    public Widget createWidget() {
        return null;
    }

    @Nullable
    public Class<? extends Widget> getWidgetClass() {
        return null;
    }

    public void applyWidgetInfo(@NotNull Widget widget,
                                int index,
                                boolean isXEI,
                                IO io,
                                @Nullable("null when storage == null") GTRecipeTypeUI.RecipeHolder recipeHolder,
                                @NotNull GTRecipeType recipeType,
                                @Nullable("null when content == null") GTRecipe recipe,
                                @Nullable Content content,
                                @Nullable Object storage, int recipeTier, int chanceTier) {}

    /**
     * Create a cache map for chanced outputs
     *
     * @return a map of this capability's content type -> integer
     */
    public Object2IntMap<T> makeChanceCache() {
        return new Object2IntOpenHashMap<>();
    }

    public boolean isTickSlot(int index, IO io, GTRecipe recipe) {
        return index >= (io == IO.IN ? recipe.getInputContents(this) : recipe.getOutputContents(this)).size();
    }
}
