package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Used to detect whether a machine has a certain capability.
 */
public abstract class RecipeCapability<T> {

    public static final Codec<RecipeCapability<?>> DIRECT_CODEC = GTRegistries.RECIPE_CAPABILITIES.codec();
//    public static final Codec<Map<RecipeCapability<?>, List<?>>> CODEC = new DispatchedMapCodec<>(
//            RecipeCapability.DIRECT_CODEC,
//            RecipeCapability::contentCodec);
    public static final Comparator<RecipeCapability<?>> COMPARATOR = Comparator.comparingInt(o -> o.sortIndex);

    public final String name;
    public final int color;
    public final boolean doRenderSlot;
    public final int sortIndex;
    public final Codec<T> contentCodec;

    protected RecipeCapability(String name, int color, boolean doRenderSlot, int sortIndex,
                               Codec<T> contentCodec) {
        this.name = name;
        this.color = color;
        this.doRenderSlot = doRenderSlot;
        this.sortIndex = sortIndex;
        this.contentCodec = contentCodec;
    }

    public static <T> Codec<List<T>> contentCodec(RecipeCapability<T> capability) {
        return capability.contentCodec.listOf();
    }

    public abstract T fromNetwork(FriendlyByteBuf friendlyByteBuf);

    public abstract void toNetwork(T ingredient,  FriendlyByteBuf friendlyByteBuf);

    public T copyInner(T content) {
        return copyInner(content, 1);
    };

    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    public abstract T copyInner(T content, int multiplier);

    /**
     * deep copy and modify the size attribute for those Content that have the size attribute.
     */
    public T copyWithMultiplier(T content, int multiplier) {
        return copyInner(content, multiplier);
    }

    @SuppressWarnings("unchecked")
    public final T copyContent(Object content) {
        return copyInner((T) content);
    }

    /**
     * used for recipe builder via KubeJs.
     */
    @SuppressWarnings("unchecked")
    public T of(Object o) {
        return (T) o;
    }

    public String slotName(IO io) {
        return "%s_%s".formatted(name, io.name().toLowerCase(Locale.ROOT));
    }

    public String slotName(IO io, int index) {
        return "%s_%s_%s".formatted(name, io.name().toLowerCase(Locale.ROOT), index);
    }

    public MutableComponent getName() {
        return Component.translatable("recipe.capability.%s.name".formatted(name));
    }

    public MutableComponent getColoredName() {
        return getName().withStyle(style -> style.withColor(this.color));
    }

    public boolean isRecipeSearchFilter() {
        return false;
    }

    public List<AbstractMapIngredient> getMapIngredients(T content) {
        return List.of();
    }

    /**
     * Does the recipe test if this capability is workable? if not, you should test validity somewhere else.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean doMatchInRecipe() {
        return true;
    }

    /**
     * Calculate the maximum parallel amount based on the output space of the holder
     *
     * @param holder        the {@link IRecipeCapabilityHolder} that contains all the inputs and outputs of the machine.
     * @param recipe        the recipe from which we get the input to product ratio
     * @param maxMultiplier the upper bound on the multiplier, see {@link #getMaxParallelByInput}
     * @param tick          whether to check regular outputs or tick outputs
     * @return the amount of times a {@link GTRecipe} outputs can be merged into an inventory without voiding products.
     */
    // returns Integer.MAX_VALUE by default, to skip processing.
    public int limitMaxParallelByOutput(RecipeHandlerGroup holder, GTRecipe recipe, int maxMultiplier,
                                        boolean tick) {
        return Integer.MAX_VALUE;
    }

    /**
     * Finds the maximum number of GTRecipes that can be performed at the same time based on the contents of input
     * inventories
     *
     * @param holder The {@link IRecipeCapabilityHolder} that contains all the inputs and outputs of the machine.
     * @param recipe The {@link GTRecipe} for which to find the maximum that can be run simultaneously
     * @param limit  The hard limit on the amount of recipes that can be performed at one time
     * @param tick   whether to check regular outputs or tick outputs
     * @return The Maximum number of GTRecipes that can be performed at a single time based on the available Items
     */
    // returns Integer.MAX_VALUE by default, to skip processing.
    public int getMaxParallelByInput(RecipeHandlerGroup holder, GTRecipe recipe, int limit, boolean tick) {
        return Integer.MAX_VALUE;
    }

    public boolean doAddGuiSlots() {
        return isRecipeSearchFilter();
    }

    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<T> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {}

    @NotNull
    public List<Object> createXEIContainerContents(List<T> contents, GTRecipe recipe, IO io) {
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

    /**
     * Return the class of the supported widget that should be used to display this capability.
     */
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
                                @Nullable T content,
                                @Nullable Object storage, int recipeTier, int chanceTier) {}

    public boolean isTickSlot(int index, IO io, GTRecipe recipe) {
        return index >= (io == IO.IN ? recipe.getInputContents(this) : recipe.getOutputContents(this)).size();
    }

    /**
     * Should this RecipeCapability bypass distinct checks?
     * E.g. should this bus be added to all recipe checks on a multi, even distinct ones like ME Pattern buffers.
     * for example: energy hatches, soul hatches, other "global per multi" hatches.
     */
    public boolean shouldBypassDistinct() {
        return true;
    }
}
