package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTRecipe implements net.minecraft.world.item.crafting.Recipe<Container> {

    public final GTRecipeType recipeType;
    @Getter
    @Setter
    public ResourceLocation id;
    public final Map<RecipeCapability<?>, List<Content>> inputs;
    public final Map<RecipeCapability<?>, List<Content>> outputs;
    public final Map<RecipeCapability<?>, List<Content>> tickInputs;
    public final Map<RecipeCapability<?>, List<Content>> tickOutputs;

    public final Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics;

    public final List<RecipeCondition> conditions;
    // for KubeJS. actual type is List<IngredientAction>.
    // Must be List<?> to not cause crashes without KubeJS.
    public final List<?> ingredientActions;
    @NotNull
    public CompoundTag data;
    public int duration;
    public int parallels = 1;
    public int subtickParallels = 1;
    public int batchParallels = 1;
    public int ocLevel = 0;
    public final GTRecipeCategory recipeCategory;
    // Lazy fields, since we need the recipe EUt very often
    @Getter(lazy = true)
    private final @NotNull EnergyStack inputEUt = calculateEUt(tickInputs);
    @Getter(lazy = true)
    private final @NotNull EnergyStack outputEUt = calculateEUt(tickOutputs);

    public GTRecipe(GTRecipeType recipeType,
                    Map<RecipeCapability<?>, List<Content>> inputs,
                    Map<RecipeCapability<?>, List<Content>> outputs,
                    Map<RecipeCapability<?>, List<Content>> tickInputs,
                    Map<RecipeCapability<?>, List<Content>> tickOutputs,
                    Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics,
                    List<RecipeCondition> conditions,
                    List<?> ingredientActions,
                    @NotNull CompoundTag data,
                    int duration,
                    @NotNull GTRecipeCategory recipeCategory) {
        this(recipeType, null, inputs, outputs, tickInputs, tickOutputs,
                inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                conditions, ingredientActions, data, duration, recipeCategory);
    }

    public GTRecipe(GTRecipeType recipeType,
                    @Nullable ResourceLocation id,
                    Map<RecipeCapability<?>, List<Content>> inputs,
                    Map<RecipeCapability<?>, List<Content>> outputs,
                    Map<RecipeCapability<?>, List<Content>> tickInputs,
                    Map<RecipeCapability<?>, List<Content>> tickOutputs,
                    Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics,
                    Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics,
                    List<RecipeCondition> conditions,
                    List<?> ingredientActions,
                    @NotNull CompoundTag data,
                    int duration,
                    @NotNull GTRecipeCategory recipeCategory) {
        this.recipeType = recipeType;
        this.id = id;

        this.inputs = inputs;
        this.outputs = outputs;
        this.tickInputs = tickInputs;
        this.tickOutputs = tickOutputs;

        this.inputChanceLogics = inputChanceLogics;
        this.outputChanceLogics = outputChanceLogics;
        this.tickInputChanceLogics = tickInputChanceLogics;
        this.tickOutputChanceLogics = tickOutputChanceLogics;

        this.conditions = conditions;
        this.ingredientActions = ingredientActions;
        this.data = data;
        this.duration = duration;
        this.recipeCategory = (recipeCategory != GTRecipeCategory.DEFAULT) ? recipeCategory : recipeType.getCategory();
    }

    public GTRecipe copy() {
        return copy(ContentModifier.IDENTITY, false);
    }

    public GTRecipe copy(ContentModifier modifier) {
        return copy(modifier, true);
    }

    public GTRecipe copy(ContentModifier modifier, boolean modifyDuration) {
        var copied = new GTRecipe(recipeType, id,
                modifier.applyContents(inputs), modifier.applyContents(outputs),
                modifier.applyContents(tickInputs), modifier.applyContents(tickOutputs),
                new HashMap<>(inputChanceLogics), new HashMap<>(outputChanceLogics),
                new HashMap<>(tickInputChanceLogics), new HashMap<>(tickOutputChanceLogics),
                new ArrayList<>(conditions),
                new ArrayList<>(ingredientActions), data, duration, recipeCategory);
        if (modifyDuration) {
            copied.duration = modifier.apply(this.duration);
        }
        copied.ocLevel = ocLevel;
        copied.parallels = parallels;
        copied.batchParallels = batchParallels;
        copied.subtickParallels = subtickParallels;
        return copied;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GTRecipeSerializer.SERIALIZER;
    }

    @Override
    public @NotNull GTRecipeType getType() {
        return recipeType;
    }

    @Override
    public boolean matches(@NotNull Container pContainer, @NotNull Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inventory, RegistryAccess registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryManager) {
        return ItemStack.EMPTY;
    }

    public List<Content> getInputContents(RecipeCapability<?> capability) {
        return inputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getOutputContents(RecipeCapability<?> capability) {
        return outputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getTickInputContents(RecipeCapability<?> capability) {
        return tickInputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getTickOutputContents(RecipeCapability<?> capability) {
        return tickOutputs.getOrDefault(capability, Collections.emptyList());
    }

    public boolean hasTick() {
        return !tickInputs.isEmpty() || !tickOutputs.isEmpty();
    }

    /**
     * Get the chance logic for a recipe capability + io + tick io combination
     *
     * @param cap the recipe capability to get the chance logic for
     * @param io  the {@link IO} of the chanche per-tick logic or the normal one
     * @return the chance logic for the aforementioned combination. Defaults to {@link ChanceLogic#OR}.
     */
    public ChanceLogic getChanceLogicForCapability(RecipeCapability<?> cap, IO io, boolean isTick) {
        if (io == IO.OUT) {
            if (isTick) {
                return tickOutputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
            } else {
                return outputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
            }
        } else if (io == IO.IN) {
            if (isTick) {
                return tickInputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
            } else {
                return inputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
            }
        }
        return ChanceLogic.OR;
    }

    // Technically should account for overflow but realistically not an issue.
    protected @NotNull EnergyStack calculateEUt(Map<RecipeCapability<?>, List<Content>> contents) {
        var outputs = contents.get(EURecipeCapability.CAP);
        if (outputs == null) return EnergyStack.EMPTY;
        long v = 0, a = 0;
        for (var content : outputs) {
            EnergyStack stack = EURecipeCapability.CAP.of(content.content);
            v += stack.voltage();
            a += stack.amperage();
        }
        return new EnergyStack(v, a);
    }

    public int getTotalRuns() {
        return parallels * subtickParallels * batchParallels;
    }

    // Just check id as there *should* only ever be 1 instance of a recipe with this id.
    // If this doesn't work, fix.
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GTRecipe recipe)) return false;
        return this.id.equals(recipe.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
