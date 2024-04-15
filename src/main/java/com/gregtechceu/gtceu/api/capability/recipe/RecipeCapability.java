package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

/**
 * Used to detect whether a machine has a certain capability.
 */
public abstract class RecipeCapability<T> {
    public static final Comparator<RecipeCapability<?>> COMPARATOR = Comparator.comparingInt(o -> o.sortIndex);

    public final String name;
    public final int color;
    public final boolean doRenderSlot;
    public final int sortIndex;
    public final IContentSerializer<T> serializer;

    protected RecipeCapability(String name, int color, boolean doRenderSlot, int sortIndex, IContentSerializer<T> serializer) {
        this.name = name;
        this.color = color;
        this.doRenderSlot = doRenderSlot;
        this.sortIndex = sortIndex;
        this.serializer = serializer;
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
    public T copyWithModifier(T content, ContentModifier modifier){
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
     * Convert the passed object to a list of recipe lookup filters.
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
     * Does the recipe test if this capability is workable? if not, you should test validity somewhere else.
     */
    public boolean doMatchInRecipe() {
        return true;
    }

    public boolean doAddGuiSlots() {
        return isRecipeSearchFilter();
    }

    public void addXEIInfo(WidgetGroup group, int xOffset, List<Content> contents, boolean perTick, boolean isInput, MutableInt yOffset) {

    }

    @NotNull
    public List<Object> createXEIContainerContents(List<Content> contents, GTRecipe recipe, IO io) {
        return new ArrayList<>();
    }

    @Nullable
    public Object createXEIContainer(List<?> contents) {
        return null;
    }

    @UnknownNullability("null when getWidgetClass() == null")
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
                                GTRecipeTypeUI.@UnknownNullability("null when storage == null") RecipeHolder recipeHolder,
                                @NotNull GTRecipeType recipeType,
                                @UnknownNullability("null when content == null") GTRecipe recipe,
                                @Nullable Content content,
                                @Nullable Object storage) {

    }

     //TODO
    public double calculateAmount(List<T> left) {
        return 1;
    }
}
