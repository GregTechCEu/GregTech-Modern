package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.codec.DispatchedMapCodec;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
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

    public Tag contentToNbt(Object value) {
        return this.serializer.toNbt(this.of(value));
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

    public MutableComponent getName() {
        return Component.translatable("recipe.capability.%s.name".formatted(name));
    }

    public MutableComponent getColoredName() {
        return getName().withStyle(style -> style.withColor(this.color));
    }

    public boolean isRecipeSearchFilter() {
        return false;
    }

    public List<Object> compressIngredients(Collection<Object> ingredients) {
        return new ArrayList<>(ingredients);
    }

    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        return null;
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
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int maxMultiplier,
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
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
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

    /**
     * Should this RecipeCapability bypass distinct checks?
     * E.g. should this bus be added to all recipe checks on a multi, even distinct ones like ME Pattern buffers.
     * for example: energy hatches, soul hatches, other "global per multi" hatches.
     */
    public boolean shouldBypassDistinct() {
        return true;
    }
}
