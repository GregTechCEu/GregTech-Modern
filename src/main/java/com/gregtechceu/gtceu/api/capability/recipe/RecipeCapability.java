package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.codec.DispatchedMapCodec;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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

    public final ResourceLocation id;
    public final int color;
    public final boolean doRenderSlot;
    public final int sortIndex;
    public final IContentSerializer<T> serializer;

    protected RecipeCapability(ResourceLocation id, int color, boolean doRenderSlot, int sortIndex,
                               IContentSerializer<T> serializer) {
        this.id = id;
        this.color = color;
        this.doRenderSlot = doRenderSlot;
        this.sortIndex = sortIndex;
        this.serializer = serializer;
    }

    /**
     * @deprecated Use {@link #RecipeCapability(ResourceLocation, int, boolean, int, IContentSerializer)}
     */
    @Deprecated(forRemoval = true, since = "8.0.0")
    protected RecipeCapability(String name, int color, boolean doRenderSlot, int sortIndex,
                               IContentSerializer<T> serializer) {
        this(GTCEu.id(name), color, doRenderSlot, sortIndex, serializer);
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
        return "%s_%s".formatted(id, io.name().toLowerCase(Locale.ROOT));
    }

    public String slotName(IO io, int index) {
        return "%s_%s_%s".formatted(id, io.name().toLowerCase(Locale.ROOT), index);
    }

    public MutableComponent getName() {
        return Component.translatable("recipe.capability.%s.name".formatted(id.getPath()));
    }

    public MutableComponent getColoredName() {
        return getName().withStyle(style -> style.withColor(this.color));
    }

    public boolean isRecipeSearchFilter() {
        return false;
    }

    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
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

    /**
     * Should handlers of this capability be tried even when {@link IRecipeHandler#getTotalContentAmount()} is zero?
     * E.g. should this capability bypass the empty handler optimization for rate-based capabilities.
     * for example: CWU, where stored amount is zero but the handler can still provide computation.
     */
    public boolean skipEmptyContentCheck() {
        return false;
    }

    /**
     * Gets all {@link NotifiableRecipeHandlerTrait} traits that can handle this capability.
     *
     * @param machine The machine to get traits from
     * @return A list containing the traits
     */
    public List<? extends NotifiableRecipeHandlerTrait<T>> getCapabilityHandlers(MetaMachine machine) {
        return List.of();
    }

    /**
     * Gets all {@link NotifiableRecipeHandlerTrait} traits with a specific IO that can handle this capability.
     *
     * @param machine The machine to get traits from
     * @param io      The handler IO of the traits
     * @return A list containing the traits
     */
    public List<? extends NotifiableRecipeHandlerTrait<T>> getCapabilityHandlers(MetaMachine machine, IO io) {
        return getCapabilityHandlers(machine).stream()
                .filter(v -> v.getHandlerIO() == io).toList();
    }
}
