package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.*;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.recipe.condition.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.utils.NBTToJsonConverter;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({ "unchecked", "UnusedReturnValue" })
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class GTRecipeBuilder {

    public final Map<RecipeCapability<?>, List<Content>> input = new IdentityHashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> tickInput = new IdentityHashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> output = new IdentityHashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> tickOutput = new IdentityHashMap<>();

    public final Map<RecipeCapability<?>, ChanceLogic> inputChanceLogic = new IdentityHashMap<>();
    public final Map<RecipeCapability<?>, ChanceLogic> outputChanceLogic = new IdentityHashMap<>();
    public final Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogic = new IdentityHashMap<>();
    public final Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogic = new IdentityHashMap<>();

    public final List<RecipeCondition> conditions = new ArrayList<>();

    @NotNull
    public CompoundTag data = new CompoundTag();
    @Setter
    public ResourceLocation id;
    @Setter
    public GTRecipeType recipeType;
    @Setter
    public int duration = 100;
    @Setter
    public boolean perTick;
    @Setter
    public String slotName;
    @Setter
    public String uiName;
    @Setter
    public int chance = ChanceLogic.getMaxChancedValue();
    @Setter
    public int maxChance = ChanceLogic.getMaxChancedValue();
    @Setter
    public int tierChanceBoost = 0;
    @Setter
    public boolean isFuel = false;
    public GTRecipeCategory recipeCategory;
    @Setter
    public BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave;
    @Getter
    private final Collection<ResearchRecipeEntry> researchRecipeEntries = new ArrayList<>();
    private boolean generatingRecipes = true;

    public GTRecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
        this.id = id;
        this.recipeType = recipeType;
        this.recipeCategory = recipeType.getCategory();
    }

    public GTRecipeBuilder(GTRecipe toCopy, GTRecipeType recipeType) {
        this.id = toCopy.id;
        this.recipeType = recipeType;
        toCopy.inputs.forEach((k, v) -> this.input.put(k, new ArrayList<>(v)));
        toCopy.outputs.forEach((k, v) -> this.output.put(k, new ArrayList<>(v)));
        toCopy.tickInputs.forEach((k, v) -> this.tickInput.put(k, new ArrayList<>(v)));
        toCopy.tickOutputs.forEach((k, v) -> this.tickOutput.put(k, new ArrayList<>(v)));
        this.inputChanceLogic.putAll(toCopy.inputChanceLogics);
        this.outputChanceLogic.putAll(toCopy.outputChanceLogics);
        this.tickInputChanceLogic.putAll(toCopy.tickInputChanceLogics);
        this.tickOutputChanceLogic.putAll(toCopy.tickOutputChanceLogics);
        this.conditions.addAll(toCopy.conditions);
        this.data = toCopy.data.copy();
        this.duration = toCopy.duration;
        this.isFuel = toCopy.isFuel;
        this.recipeCategory = toCopy.recipeCategory;
    }

    public static GTRecipeBuilder of(ResourceLocation id, GTRecipeType recipeType) {
        return new GTRecipeBuilder(id, recipeType);
    }

    public static GTRecipeBuilder ofRaw() {
        return new GTRecipeBuilder(GTCEu.id("raw"), GTRecipeTypes.DUMMY_RECIPES);
    }

    public GTRecipeBuilder copy(String id) {
        return copy(GTCEu.id(id));
    }

    public GTRecipeBuilder copy(ResourceLocation id) {
        GTRecipeBuilder copy = new GTRecipeBuilder(id, this.recipeType);
        this.input.forEach((k, v) -> copy.input.put(k, new ArrayList<>(v)));
        this.output.forEach((k, v) -> copy.output.put(k, new ArrayList<>(v)));
        this.tickInput.forEach((k, v) -> copy.tickInput.put(k, new ArrayList<>(v)));
        this.tickOutput.forEach((k, v) -> copy.tickOutput.put(k, new ArrayList<>(v)));
        copy.inputChanceLogic.putAll(this.inputChanceLogic);
        copy.outputChanceLogic.putAll(this.outputChanceLogic);
        copy.tickInputChanceLogic.putAll(this.tickInputChanceLogic);
        copy.tickOutputChanceLogic.putAll(this.tickOutputChanceLogic);
        copy.conditions.addAll(this.conditions);
        copy.data = this.data.copy();
        copy.duration = this.duration;
        copy.chance = this.chance;
        copy.perTick = this.perTick;
        copy.isFuel = this.isFuel;
        copy.recipeCategory = this.recipeCategory;
        copy.uiName = this.uiName;
        copy.slotName = this.slotName;
        copy.onSave = this.onSave;
        return copy;
    }

    public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return builder.copy(builder.id).onSave(null).recipeType(recipeType).category(recipeCategory);
    }

    public <T> GTRecipeBuilder input(RecipeCapability<T> capability, T obj) {
        var t = (perTick ? tickInput : input);
        if (t.get(capability) != null && t.get(capability).size() >= recipeType.getMaxInputs(capability)) {
            GTCEu.LOGGER.warn("Trying to add more inputs than RecipeType can support, id: {}, Max {}{}Inputs: {}",
                    id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxInputs(capability));
        }
        t.computeIfAbsent(capability, c -> new ArrayList<>())
                .add(new Content(capability.of(obj), chance, maxChance, tierChanceBoost, slotName, uiName));
        return this;
    }

    public <T> GTRecipeBuilder input(RecipeCapability<T> capability, T... obj) {
        var t = (perTick ? tickInput : input);
        if (t.get(capability) != null && t.get(capability).size() + obj.length > recipeType.getMaxInputs(capability)) {
            GTCEu.LOGGER.warn("Trying to add more inputs than RecipeType can support, id: {}, Max {}{}Inputs: {}",
                    id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxInputs(capability));
        }
        (perTick ? tickInput : input).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, maxChance, tierChanceBoost, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder output(RecipeCapability<T> capability, T obj) {
        var t = (perTick ? tickOutput : output);
        if (t.get(capability) != null && t.get(capability).size() >= recipeType.getMaxOutputs(capability)) {
            GTCEu.LOGGER.warn("Trying to add more outputs than RecipeType can support, id: {}, Max {}{}Outputs: {}",
                    id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxOutputs(capability));
        }
        (perTick ? tickOutput : output).computeIfAbsent(capability, c -> new ArrayList<>())
                .add(new Content(capability.of(obj), chance, maxChance, tierChanceBoost, slotName, uiName));
        return this;
    }

    public <T> GTRecipeBuilder output(RecipeCapability<T> capability, T... obj) {
        var t = (perTick ? tickOutput : output);
        if (t.get(capability) != null && t.get(capability).size() + obj.length > recipeType.getMaxOutputs(capability)) {
            GTCEu.LOGGER.warn("Trying to add more outputs than RecipeType can support, id: {}, Max {}{}Outputs: {}",
                    id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxOutputs(capability));
        }
        (perTick ? tickOutput : output).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, maxChance, tierChanceBoost, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder inputs(RecipeCapability<T> capability, Object obj) {
        var t = (perTick ? tickInput : input);
        if (t.get(capability) != null && t.get(capability).size() >= recipeType.getMaxInputs(capability)) {
            GTCEu.LOGGER.warn("Trying to add more inputs than RecipeType can support, id: {}, Max {}{}Inputs: {}",
                    id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxInputs(capability));
        }
        (perTick ? tickInput : input).computeIfAbsent(capability, c -> new ArrayList<>())
                .add(new Content(capability.of(obj), chance, maxChance, tierChanceBoost, slotName, uiName));
        return this;
    }

    public <T> GTRecipeBuilder inputs(RecipeCapability<T> capability, Object... obj) {
        var t = (perTick ? tickInput : input);
        if (t.get(capability) != null && t.get(capability).size() + obj.length > recipeType.getMaxInputs(capability)) {
            GTCEu.LOGGER.warn("Trying to add more inputs than RecipeType can support, id: {}, Max {}{}Inputs: {}",
                    id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxInputs(capability));
        }
        (perTick ? tickInput : input).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, maxChance, tierChanceBoost, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder outputs(RecipeCapability<T> capability, Object obj) {
        var t = (perTick ? tickOutput : output);
        if (t.get(capability) != null && t.get(capability).size() >= recipeType.getMaxOutputs(capability)) {
            GTCEu.LOGGER.warn("Trying to add more outputs than RecipeType can support, id: {}, Max {}{}Outputs: {}",
                    id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxOutputs(capability));
        }
        (perTick ? tickOutput : output).computeIfAbsent(capability, c -> new ArrayList<>())
                .add(new Content(capability.of(obj), chance, maxChance, tierChanceBoost, slotName, uiName));
        return this;
    }

    public <T> GTRecipeBuilder outputs(RecipeCapability<T> capability, Object... obj) {
        var t = (perTick ? tickOutput : output);
        if (t.get(capability) != null && t.get(capability).size() + obj.length > recipeType.getMaxOutputs(capability)) {
            GTCEu.LOGGER.warn("Trying to add more outputs than RecipeType can support, id: {}, Max {}{}Outputs: {}",
                    id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxOutputs(capability));
        }
        (perTick ? tickOutput : output).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, maxChance, tierChanceBoost, slotName, uiName)).toList());
        return this;
    }

    public GTRecipeBuilder addCondition(RecipeCondition condition) {
        conditions.add(condition);
        return this;
    }

    public GTRecipeBuilder inputEU(long eu) {
        return input(EURecipeCapability.CAP, eu);
    }

    public GTRecipeBuilder EUt(long eu) {
        if (eu == 0) {
            GTCEu.LOGGER.error("EUt can't be explicitly set to 0, id: {}", id);
        }
        var lastPerTick = perTick;
        perTick = true;
        if (eu > 0) {
            tickInput.remove(EURecipeCapability.CAP);
            inputEU(eu);
        } else if (eu < 0) {
            tickOutput.remove(EURecipeCapability.CAP);
            outputEU(-eu);
        }
        perTick = lastPerTick;
        return this;
    }

    public GTRecipeBuilder outputEU(long eu) {
        return output(EURecipeCapability.CAP, eu);
    }

    public GTRecipeBuilder inputCWU(int cwu) {
        return input(CWURecipeCapability.CAP, cwu);
    }

    public GTRecipeBuilder CWUt(int cwu) {
        if (cwu == 0) {
            GTCEu.LOGGER.error("CWUt can't be explicitly set to 0, id: {}", id);
        }
        var lastPerTick = perTick;
        perTick = true;
        if (cwu > 0) {
            tickInput.remove(CWURecipeCapability.CAP);
            inputCWU(cwu);
        } else if (cwu < 0) {
            tickOutput.remove(CWURecipeCapability.CAP);
            outputCWU(cwu);
        }
        perTick = lastPerTick;
        return this;
    }

    public GTRecipeBuilder totalCWU(int cwu) {
        this.durationIsTotalCWU(true);
        this.hideDuration(true);
        this.duration(cwu);
        return this;
    }

    public GTRecipeBuilder outputCWU(int cwu) {
        return output(CWURecipeCapability.CAP, cwu);
    }

    public GTRecipeBuilder inputItems(Object input) {
        if (input instanceof Item item) {
            return inputItems(item);
        } else if (input instanceof Supplier<?> supplier && supplier.get() instanceof ItemLike item) {
            return inputItems(item.asItem());
        } else if (input instanceof ItemStack stack) {
            return inputItems(stack);
        } else if (input instanceof Ingredient ingredient) {
            return inputItems(ingredient);
        } else if (input instanceof UnificationEntry entry) {
            return inputItems(entry);
        } else if (input instanceof TagKey<?> tag) {
            return inputItems((TagKey<Item>) tag);
        } else if (input instanceof MachineDefinition machine) {
            return inputItems(machine);
        } else {
            GTCEu.LOGGER.error(
                    "Input item is not one of: Item, Supplier<Item>, ItemStack, Ingredient, UnificationEntry, TagKey<Item>, MachineDefinition, id: {}",
                    id);
            return this;
        }
    }

    public GTRecipeBuilder inputItems(Object input, int count) {
        if (input instanceof Item item) {
            return inputItems(item, count);
        } else if (input instanceof Supplier<?> supplier && supplier.get() instanceof ItemLike item) {
            return inputItems(item.asItem(), count);
        } else if (input instanceof ItemStack stack) {
            return inputItems(stack.copyWithCount(count));
        } else if (input instanceof Ingredient ingredient) {
            return inputItems(ingredient, count);
        } else if (input instanceof UnificationEntry entry) {
            return inputItems(entry, count);
        } else if (input instanceof TagKey<?> tag) {
            return inputItems((TagKey<Item>) tag, count);
        } else if (input instanceof MachineDefinition machine) {
            return inputItems(machine, count);
        } else {
            GTCEu.LOGGER.error(
                    "Input item is not one of: Item, Supplier<Item>, ItemStack, Ingredient, UnificationEntry, TagKey<Item>, MachineDefinition, id: {}",
                    id);
            return this;
        }
    }

    public GTRecipeBuilder inputItems(Ingredient inputs) {
        return input(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder inputItems(Ingredient... inputs) {
        return input(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder inputItems(ItemStack input) {
        if (input.isEmpty()) {
            GTCEu.LOGGER.error("Input items is empty, id: {}", id);
        }
        return input(ItemRecipeCapability.CAP, SizedIngredient.create(input));
    }

    public GTRecipeBuilder inputItems(ItemStack... inputs) {
        for (ItemStack itemStack : inputs) {
            if (itemStack.isEmpty()) {
                GTCEu.LOGGER.error("Input item is empty, id: {}", id);
            }
        }
        return input(ItemRecipeCapability.CAP,
                Arrays.stream(inputs).map(SizedIngredient::create).toArray(Ingredient[]::new));
    }

    public GTRecipeBuilder inputItems(TagKey<Item> tag, int amount) {
        if (amount == 0) {
            GTCEu.LOGGER.error("Item Count is 0, id: {}", id);
        }
        return inputItems(SizedIngredient.create(tag, amount));
    }

    public GTRecipeBuilder inputItems(TagKey<Item> tag) {
        return inputItems(tag, 1);
    }

    public GTRecipeBuilder inputItems(Item input, int amount) {
        return inputItems(new ItemStack(input, amount));
    }

    public GTRecipeBuilder inputItems(Item input) {
        return inputItems(SizedIngredient.create(new ItemStack(input)));
    }

    public GTRecipeBuilder inputItems(Supplier<? extends Item> input) {
        return inputItems(input.get());
    }

    public GTRecipeBuilder inputItems(Supplier<? extends Item> input, int amount) {
        return inputItems(new ItemStack(input.get(), amount));
    }

    public GTRecipeBuilder inputItems(TagPrefix orePrefix, Material material) {
        return inputItems(orePrefix, material, 1);
    }

    public GTRecipeBuilder inputItems(UnificationEntry input) {
        if (input.material == null) {
            GTCEu.LOGGER.error("Unification Entry material is null, id: {}, TagPrefix: {}", id, input.tagPrefix);
        }
        return inputItems(input.tagPrefix, input.material, 1);
    }

    public GTRecipeBuilder inputItems(UnificationEntry input, int count) {
        if (input.material == null) {
            GTCEu.LOGGER.error("Unification Entry material is null, id: {}, TagPrefix: {}", id, input.tagPrefix);
        }
        return inputItems(input.tagPrefix, input.material, count);
    }

    public GTRecipeBuilder inputItems(TagPrefix orePrefix, Material material, int count) {
        TagKey<Item> tag = ChemicalHelper.getTag(orePrefix, material);
        if (tag == null) {
            var item = ChemicalHelper.get(orePrefix, material, count);
            if (item.isEmpty()) {
                GTCEu.LOGGER.error(
                        "Tried to set input item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}",
                        id, orePrefix, material, count);
            }
            return inputItems(item);
        }
        return inputItems(tag, count);
    }

    public GTRecipeBuilder inputItems(MachineDefinition machine) {
        return inputItems(machine, 1);
    }

    public GTRecipeBuilder inputItems(MachineDefinition machine, int count) {
        return inputItems(machine.asStack(count));
    }

    public GTRecipeBuilder outputItems(Object input) {
        if (input instanceof Item item) {
            return outputItems(item);
        } else if (input instanceof Supplier<?> supplier && supplier.get() instanceof ItemLike item) {
            return outputItems(item.asItem());
        } else if (input instanceof ItemStack stack) {
            return outputItems(stack);
        } else if (input instanceof UnificationEntry entry) {
            return outputItems(entry);
        } else if (input instanceof MachineDefinition machine) {
            return outputItems(machine);
        } else {
            GTCEu.LOGGER.error(
                    "Output item is not one of: Item, Supplier<Item>, ItemStack, Ingredient, UnificationEntry, TagKey<Item>, MachineDefinition, id: {}",
                    id);
            return this;
        }
    }

    public GTRecipeBuilder outputItems(Object input, int count) {
        if (input instanceof Item item) {
            return outputItems(item, count);
        } else if (input instanceof Supplier<?> supplier && supplier.get() instanceof ItemLike item) {
            return outputItems(item.asItem(), count);
        } else if (input instanceof ItemStack stack) {
            return outputItems(stack.copyWithCount(count));
        } else if (input instanceof UnificationEntry entry) {
            return outputItems(entry, count);
        } else if (input instanceof MachineDefinition machine) {
            return outputItems(machine, count);
        } else {
            GTCEu.LOGGER.error(
                    "Output item is not one of: Item, Supplier<Item>, ItemStack, Ingredient, UnificationEntry, TagKey<Item>, MachineDefinition, id: {}",
                    id);
            return this;
        }
    }

    public GTRecipeBuilder outputItems(Ingredient... inputs) {
        return output(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder outputItems(ItemStack output) {
        if (output.isEmpty()) {
            GTCEu.LOGGER.error("Output items is empty, id: {}", id);
        }
        return output(ItemRecipeCapability.CAP, SizedIngredient.create(output));
    }

    public GTRecipeBuilder outputItems(ItemStack... outputs) {
        for (ItemStack itemStack : outputs) {
            if (itemStack.isEmpty()) {
                GTCEu.LOGGER.error("Output items is empty, id: {}", id);
            }
        }
        return output(ItemRecipeCapability.CAP,
                Arrays.stream(outputs).map(SizedIngredient::create).toArray(Ingredient[]::new));
    }

    public GTRecipeBuilder outputItems(Item output, int amount) {
        return outputItems(new ItemStack(output, amount));
    }

    public GTRecipeBuilder outputItems(Item output) {
        return outputItems(new ItemStack(output));
    }

    public GTRecipeBuilder outputItems(Supplier<? extends ItemLike> input) {
        return outputItems(new ItemStack(input.get().asItem()));
    }

    public GTRecipeBuilder outputItems(Supplier<? extends ItemLike> input, int amount) {
        return outputItems(new ItemStack(input.get().asItem(), amount));
    }

    public GTRecipeBuilder outputItems(TagPrefix orePrefix, Material material) {
        return outputItems(orePrefix, material, 1);
    }

    public GTRecipeBuilder outputItems(TagPrefix orePrefix, Material material, int count) {
        var item = ChemicalHelper.get(orePrefix, material, count);
        if (item.isEmpty()) {
            GTCEu.LOGGER.error("Tried to set output item stack that doesn't exist, TagPrefix: {}, Material: {}",
                    orePrefix, material);
        }
        return outputItems(item);
    }

    public GTRecipeBuilder outputItems(UnificationEntry entry) {
        if (entry.material == null) {
            GTCEu.LOGGER.error("Unification Entry material is null, id: {}, TagPrefix: {}", id, entry.tagPrefix);
        }
        return outputItems(entry.tagPrefix, entry.material);
    }

    public GTRecipeBuilder outputItems(UnificationEntry entry, int count) {
        if (entry.material == null) {
            GTCEu.LOGGER.error("Unification Entry material is null, id: {}, TagPrefix: {}", id, entry.tagPrefix);
        }
        return outputItems(entry.tagPrefix, entry.material, count);
    }

    public GTRecipeBuilder outputItems(MachineDefinition machine) {
        return outputItems(machine, 1);
    }

    public GTRecipeBuilder outputItems(MachineDefinition machine, int count) {
        return outputItems(machine.asStack(count));
    }

    public GTRecipeBuilder outputItemsRanged(ItemStack output, IntProvider intProvider) {
        return outputItems(IntProviderIngredient.create(SizedIngredient.create(output), intProvider));
    }

    public GTRecipeBuilder outputItemsRanged(Item input, IntProvider intProvider) {
        return outputItemsRanged(new ItemStack(input), intProvider);
    }

    public GTRecipeBuilder outputItemsRanged(Supplier<? extends ItemLike> output, IntProvider intProvider) {
        return outputItemsRanged(new ItemStack(output.get().asItem()), intProvider);
    }

    public GTRecipeBuilder outputItemsRanged(TagPrefix orePrefix, Material material, IntProvider intProvider) {
        var item = ChemicalHelper.get(orePrefix, material, 1);
        if (item.isEmpty()) {
            GTCEu.LOGGER.error("Tried to set output ranged item stack that doesn't exist, TagPrefix: {}, Material: {}",
                    orePrefix, material);
        }
        return outputItemsRanged(item, intProvider);
    }

    public GTRecipeBuilder outputItemsRanged(MachineDefinition machine, IntProvider intProvider) {
        return outputItemsRanged(machine.asStack(), intProvider);
    }

    public GTRecipeBuilder notConsumable(ItemStack itemStack) {
        int lastChance = this.chance;
        this.chance = 0;
        inputItems(itemStack);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder notConsumable(Ingredient ingredient) {
        int lastChance = this.chance;
        this.chance = 0;
        inputItems(ingredient);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder notConsumable(Item item) {
        int lastChance = this.chance;
        this.chance = 0;
        inputItems(item);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder notConsumable(Supplier<? extends Item> item) {
        int lastChance = this.chance;
        this.chance = 0;
        inputItems(item);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder notConsumable(TagPrefix orePrefix, Material material) {
        int lastChance = this.chance;
        this.chance = 0;
        inputItems(orePrefix, material);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder notConsumable(TagPrefix orePrefix, Material material, int count) {
        int lastChance = this.chance;
        this.chance = 0;
        inputItems(orePrefix, material, count);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder notConsumableFluid(FluidStack fluid) {
        return notConsumableFluid(FluidIngredient.of(
                TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath()), fluid.getAmount()));
    }

    public GTRecipeBuilder notConsumableFluid(FluidIngredient ingredient) {
        int lastChance = this.chance;
        this.chance = 0;
        inputFluids(ingredient);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder circuitMeta(int configuration) {
        if (configuration < 0 || configuration > IntCircuitBehaviour.CIRCUIT_MAX) {
            GTCEu.LOGGER.error("Circuit configuration must be in the bounds 0 - 32");
        }
        return notConsumable(IntCircuitIngredient.circuitInput(configuration));
    }

    public GTRecipeBuilder chancedInput(ItemStack stack, int chance, int tierChanceBoost) {
        if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), chance, new Throwable());
            return this;
        }
        int lastChance = this.chance;
        int lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance;
        this.tierChanceBoost = tierChanceBoost;
        inputItems(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public GTRecipeBuilder chancedInput(FluidStack stack, int chance, int tierChanceBoost) {
        if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), chance, new Throwable());
            return this;
        }
        int lastChance = this.chance;
        int lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance;
        this.tierChanceBoost = tierChanceBoost;
        inputFluids(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public GTRecipeBuilder chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
        if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), chance, new Throwable());
            return this;
        }
        int lastChance = this.chance;
        int lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance;
        this.tierChanceBoost = tierChanceBoost;
        outputItems(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public GTRecipeBuilder chancedOutput(FluidStack stack, int chance, int tierChanceBoost) {
        if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), chance, new Throwable());
            return this;
        }
        int lastChance = this.chance;
        int lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance;
        this.tierChanceBoost = tierChanceBoost;
        outputFluids(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int chance, int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(tag, mat), chance, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(tag, mat, count), chance, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(ItemStack stack, String fraction, int tierChanceBoost) {
        if (stack.isEmpty()) {
            return this;
        }

        String[] split = fraction.split("/");
        if (split.length != 2) {
            GTCEu.LOGGER.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".",
                    fraction, new Throwable());
            return this;
        }

        int chance;
        int maxChance;
        try {
            chance = Integer.parseInt(split[0]);
            maxChance = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            GTCEu.LOGGER.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".",
                    fraction, new Throwable());
            return this;
        }

        if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), chance, new Throwable());
            return this;
        }
        if (chance >= maxChance || maxChance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Max Chance cannot be less or equal to Chance or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), maxChance, new Throwable());
            return this;
        }

        int scalar = Math.floorDiv(ChanceLogic.getMaxChancedValue(), maxChance);
        chance *= scalar;
        maxChance *= scalar;

        int lastChance = this.chance;
        int lastMaxChance = this.maxChance;
        int lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance;
        this.maxChance = maxChance;
        this.tierChanceBoost = tierChanceBoost;
        outputItems(stack);
        this.chance = lastChance;
        this.maxChance = lastMaxChance;
        this.tierChanceBoost = lastTierChanceBoost;

        return this;
    }

    public GTRecipeBuilder chancedOutput(TagPrefix prefix, Material material, int count, String fraction,
                                         int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(prefix, material, count), fraction, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(TagPrefix prefix, Material material, String fraction, int tierChanceBoost) {
        return chancedOutput(prefix, material, 1, fraction, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(Item item, int count, String fraction, int tierChanceBoost) {
        return chancedOutput(new ItemStack(item, count), fraction, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(Item item, String fraction, int tierChanceBoost) {
        return chancedOutput(item, 1, fraction, tierChanceBoost);
    }

    public GTRecipeBuilder chancedFluidOutput(FluidStack stack, String fraction, int tierChanceBoost) {
        if (stack.isEmpty()) {
            return this;
        }

        String[] split = fraction.split("/");
        if (split.length != 2) {
            GTCEu.LOGGER.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".",
                    fraction, new Throwable());
            return this;
        }

        int chance;
        int maxChance;
        try {
            chance = Integer.parseInt(split[0]);
            maxChance = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            GTCEu.LOGGER.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".",
                    fraction, new Throwable());
            return this;
        }

        if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), chance, new Throwable());
            return this;
        }
        if (chance >= maxChance || maxChance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Max Chance cannot be less or equal to Chance or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), maxChance, new Throwable());
            return this;
        }

        int scalar = Math.floorDiv(ChanceLogic.getMaxChancedValue(), maxChance);
        chance *= scalar;
        maxChance *= scalar;

        int lastChance = this.chance;
        int lastMaxChance = this.maxChance;
        int lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance;
        this.maxChance = maxChance;
        this.tierChanceBoost = tierChanceBoost;
        outputFluids(stack);
        this.chance = lastChance;
        this.maxChance = lastMaxChance;
        this.tierChanceBoost = lastTierChanceBoost;

        return this;
    }

    /**
     * Set a chanced output logic for a specific capability.
     * all capabilities default to OR logic if not set.
     *
     * @param cap   the {@link RecipeCapability} to set the logic for
     * @param logic the {@link ChanceLogic} to use
     * @return this builder
     */
    public GTRecipeBuilder chancedOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        this.outputChanceLogic.put(cap, logic);
        return this;
    }

    public GTRecipeBuilder chancedItemOutputLogic(ChanceLogic logic) {
        return chancedOutputLogic(ItemRecipeCapability.CAP, logic);
    }

    public GTRecipeBuilder chancedFluidOutputLogic(ChanceLogic logic) {
        return chancedOutputLogic(FluidRecipeCapability.CAP, logic);
    }

    public GTRecipeBuilder chancedInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        this.inputChanceLogic.put(cap, logic);
        return this;
    }

    public GTRecipeBuilder chancedItemInputLogic(ChanceLogic logic) {
        return chancedInputLogic(ItemRecipeCapability.CAP, logic);
    }

    public GTRecipeBuilder chancedFluidInputLogic(ChanceLogic logic) {
        return chancedInputLogic(FluidRecipeCapability.CAP, logic);
    }

    public GTRecipeBuilder chancedTickOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        this.tickOutputChanceLogic.put(cap, logic);
        return this;
    }

    public GTRecipeBuilder chancedTickInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
        this.tickInputChanceLogic.put(cap, logic);
        return this;
    }

    public GTRecipeBuilder inputFluids(FluidStack input) {
        return input(FluidRecipeCapability.CAP, FluidIngredient.of(
                TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(input.getFluid()).getPath()),
                input.getAmount(), input.getTag()));
    }

    public GTRecipeBuilder inputFluids(FluidStack... inputs) {
        return input(FluidRecipeCapability.CAP, Arrays.stream(inputs).map(fluid -> FluidIngredient.of(
                TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath()),
                fluid.getAmount(), fluid.getTag())).toArray(FluidIngredient[]::new));
    }

    public GTRecipeBuilder inputFluids(FluidIngredient... inputs) {
        return input(FluidRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder outputFluids(FluidStack output) {
        return output(FluidRecipeCapability.CAP, FluidIngredient.of(output));
    }

    public GTRecipeBuilder outputFluids(FluidStack... outputs) {
        return output(FluidRecipeCapability.CAP,
                Arrays.stream(outputs).map(FluidIngredient::of).toArray(FluidIngredient[]::new));
    }

    public GTRecipeBuilder outputFluids(FluidIngredient... outputs) {
        return output(FluidRecipeCapability.CAP, outputs);
    }

    //////////////////////////////////////
    // ********** DATA ***********//
    //////////////////////////////////////
    public GTRecipeBuilder addData(String key, Tag data) {
        this.data.put(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, int data) {
        this.data.putInt(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, long data) {
        this.data.putLong(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, String data) {
        this.data.putString(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, Float data) {
        this.data.putFloat(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, boolean data) {
        this.data.putBoolean(key, data);
        return this;
    }

    public GTRecipeBuilder blastFurnaceTemp(int blastTemp) {
        return addData("ebf_temp", blastTemp);
    }

    public GTRecipeBuilder explosivesAmount(int explosivesAmount) {
        return inputItems(new ItemStack(Blocks.TNT, explosivesAmount));
    }

    public GTRecipeBuilder explosivesType(ItemStack explosivesType) {
        return inputItems(explosivesType);
    }

    public GTRecipeBuilder solderMultiplier(int multiplier) {
        return addData("solder_multiplier", multiplier);
    }

    public GTRecipeBuilder disableDistilleryRecipes(boolean flag) {
        return addData("disable_distillery", flag);
    }

    public GTRecipeBuilder fusionStartEU(long eu) {
        return addData("eu_to_start", eu);
    }

    public GTRecipeBuilder researchScan(boolean isScan) {
        return addData("scan_for_research", isScan);
    }

    public GTRecipeBuilder durationIsTotalCWU(boolean durationIsTotalCWU) {
        return addData("duration_is_total_cwu", durationIsTotalCWU);
    }

    public GTRecipeBuilder hideDuration(boolean hideDuration) {
        return addData("hide_duration", hideDuration);
    }

    //////////////////////////////////////
    // ******* CONDITIONS ********//
    //////////////////////////////////////

    public GTRecipeBuilder cleanroom(CleanroomType cleanroomType) {
        return addCondition(new CleanroomCondition(cleanroomType));
    }

    public GTRecipeBuilder dimension(ResourceLocation dimension, boolean reverse) {
        return addCondition(new DimensionCondition(dimension).setReverse(reverse));
    }

    public GTRecipeBuilder dimension(ResourceLocation dimension) {
        return dimension(dimension, false);
    }

    public GTRecipeBuilder biome(ResourceLocation biome, boolean reverse) {
        return addCondition(new BiomeCondition(biome).setReverse(reverse));
    }

    public GTRecipeBuilder biome(ResourceLocation biome) {
        return biome(biome, false);
    }

    public GTRecipeBuilder rain(float level, boolean reverse) {
        return addCondition(new RainingCondition(level).setReverse(reverse));
    }

    public GTRecipeBuilder rain(float level) {
        return rain(level, false);
    }

    public GTRecipeBuilder thunder(float level, boolean reverse) {
        return addCondition(new ThunderCondition(level).setReverse(reverse));
    }

    public GTRecipeBuilder thunder(float level) {
        return thunder(level, false);
    }

    public GTRecipeBuilder posY(int min, int max, boolean reverse) {
        return addCondition(new PositionYCondition(min, max).setReverse(reverse));
    }

    public GTRecipeBuilder posY(int min, int max) {
        return posY(min, max, false);
    }

    public GTRecipeBuilder environmentalHazard(MedicalCondition condition, boolean reverse) {
        return addCondition(new EnvironmentalHazardCondition(condition).setReverse(reverse));
    }

    public GTRecipeBuilder environmentalHazard(MedicalCondition condition) {
        return environmentalHazard(condition, false);
    }

    public GTRecipeBuilder daytime(boolean isNight) {
        return addCondition(new DaytimeCondition().setReverse(isNight));
    }

    public GTRecipeBuilder daytime() {
        return daytime(false);
    }

    private boolean applyResearchProperty(ResearchData.ResearchEntry researchEntry) {
        if (!ConfigHolder.INSTANCE.machines.enableResearch) return false;
        if (researchEntry == null) {
            GTCEu.LOGGER.error("Research Entry cannot be empty.", new IllegalArgumentException());
            return false;
        }

        if (!generatingRecipes) {
            GTCEu.LOGGER.error("Cannot generate recipes when using researchWithoutRecipe()",
                    new IllegalArgumentException());
            return false;
        }

        ResearchCondition condition = this.conditions.stream().filter(ResearchCondition.class::isInstance).findAny()
                .map(ResearchCondition.class::cast).orElse(null);
        if (condition != null) {
            condition.data.add(researchEntry);
        } else {
            condition = new ResearchCondition();
            condition.data.add(researchEntry);
            this.addCondition(condition);
        }
        return true;
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @return this
     */
    public GTRecipeBuilder researchWithoutRecipe(@NotNull String researchId) {
        return researchWithoutRecipe(researchId, ResearchManager.getDefaultScannerItem());
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @param dataStack  the stack to hold the data. Must have the {@link IDataItem} behavior.
     * @return this
     */
    public GTRecipeBuilder researchWithoutRecipe(@NotNull String researchId, @NotNull ItemStack dataStack) {
        applyResearchProperty(new ResearchData.ResearchEntry(researchId, dataStack));
        this.generatingRecipes = false;
        return this;
    }

    /**
     * Generates a research recipe for the Scanner.
     */
    public GTRecipeBuilder scannerResearch(UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder> research) {
        ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.ScannerRecipeBuilder()).build();
        if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId, entry.dataStack))) {
            this.researchRecipeEntries.add(entry);
        }
        return this;
    }

    /**
     * Generates a research recipe for the Scanner. All values are defaults other than the research stack.
     *
     * @param researchStack the stack to use for research
     * @return this
     */
    public GTRecipeBuilder scannerResearch(@NotNull ItemStack researchStack) {
        return scannerResearch(b -> b.researchStack(researchStack));
    }

    /**
     * Generates a research recipe for the Research Station.
     */
    public GTRecipeBuilder stationResearch(UnaryOperator<ResearchRecipeBuilder.StationRecipeBuilder> research) {
        ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.StationRecipeBuilder()).build();
        if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId, entry.dataStack))) {
            this.researchRecipeEntries.add(entry);
        }
        return this;
    }

    public GTRecipeBuilder category(@NotNull GTRecipeCategory category) {
        this.recipeCategory = category;
        return this;
    }

    public void toJson(JsonObject json) {
        json.addProperty("type", recipeType.registryName.toString());
        json.addProperty("duration", Math.abs(duration));
        if (data != null && !data.isEmpty()) {
            json.add("data", NBTToJsonConverter.getObject(data));
        }
        json.add("inputs", capabilitiesToJson(input));
        json.add("outputs", capabilitiesToJson(output));
        json.add("tickInputs", capabilitiesToJson(tickInput));
        json.add("tickOutputs", capabilitiesToJson(tickOutput));

        json.add("inputChanceLogics", chanceLogicsToJson(inputChanceLogic));
        json.add("outputChanceLogics", chanceLogicsToJson(outputChanceLogic));
        json.add("tickInputChanceLogics", chanceLogicsToJson(tickInputChanceLogic));
        json.add("tickOutputChanceLogics", chanceLogicsToJson(tickOutputChanceLogic));

        json.addProperty("category", recipeCategory.registryKey.toString());

        if (!conditions.isEmpty()) {
            JsonArray array = new JsonArray();
            for (RecipeCondition condition : conditions) {
                var condJson = condition.serialize();
                condJson.addProperty("type", GTRegistries.RECIPE_CONDITIONS.getKey(condition.getType()));
                array.add(condJson);
            }
            json.add("recipeConditions", array);
        }
        if (isFuel) {
            json.addProperty("isFuel", true);
        }
    }

    public JsonObject capabilitiesToJson(Map<RecipeCapability<?>, List<Content>> contents) {
        JsonObject jsonObject = new JsonObject();
        contents.forEach((cap, list) -> {
            JsonArray contentsJson = new JsonArray();
            for (Content content : list) {
                contentsJson.add(cap.serializer.toJsonContent(content));
            }
            jsonObject.add(GTRegistries.RECIPE_CAPABILITIES.getKey(cap), contentsJson);
        });
        return jsonObject;
    }

    public JsonObject chanceLogicsToJson(Map<RecipeCapability<?>, ChanceLogic> chanceLogics) {
        JsonObject jsonObject = new JsonObject();
        chanceLogics.forEach((cap, logic) -> {
            String capId = GTRegistries.RECIPE_CAPABILITIES.getKey(cap);
            String logicId = GTRegistries.CHANCE_LOGICS.getKey(logic);
            jsonObject.addProperty(capId, logicId);
        });
        return jsonObject;
    }

    public FinishedRecipe build() {
        return new FinishedRecipe() {

            @Override
            public void serializeRecipeData(JsonObject pJson) {
                toJson(pJson);
            }

            @Override
            public ResourceLocation getId() {
                return new ResourceLocation(id.getNamespace(), recipeType.registryName.getPath() + "/" + id.getPath());
            }

            @Override
            public RecipeSerializer<?> getType() {
                return GTRecipeSerializer.SERIALIZER;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        };
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        if (onSave != null) {
            onSave.accept(this, consumer);
        }
        ResearchCondition condition = this.conditions.stream().filter(ResearchCondition.class::isInstance).findAny()
                .map(ResearchCondition.class::cast).orElse(null);
        if (condition != null) {
            for (ResearchData.ResearchEntry entry : condition.data) {
                this.recipeType.addDataStickEntry(entry.getResearchId(), buildRawRecipe());
            }
        }

        if (recipeType != null) {
            if (recipeCategory == null) {
                GTCEu.LOGGER.error("Recipes must have a category", new IllegalArgumentException());
            } else if (recipeCategory != GTRecipeCategory.DEFAULT && recipeCategory.getRecipeType() != recipeType) {
                GTCEu.LOGGER.error("Cannot apply Category with incompatible RecipeType",
                        new IllegalArgumentException());
            }
        }

        consumer.accept(build());
    }

    public GTRecipe buildRawRecipe() {
        var recipe = new GTRecipe(recipeType, id.withPrefix(recipeType.registryName.getPath() + "/"),
                input, output, tickInput, tickOutput,
                inputChanceLogic, outputChanceLogic, tickInputChanceLogic, tickOutputChanceLogic,
                conditions, List.of(), data, duration, isFuel, recipeCategory);
        return recipe;
    }

    //////////////////////////////////////
    // ******* Quick Query *******//
    //////////////////////////////////////
    public long EUt() {
        if (!tickInput.containsKey(EURecipeCapability.CAP)) return 0;
        if (tickInput.get(EURecipeCapability.CAP).isEmpty()) return 0;
        return EURecipeCapability.CAP.of(tickInput.get(EURecipeCapability.CAP).get(0).content);
    }

    public int getSolderMultiplier() {
        if (data.contains("solderMultiplier")) {
            return Math.max(1, data.getInt("solderMultiplier"));
        }
        return Math.max(1, data.getInt("solder_multiplier"));
    }

    /**
     * An entry for an autogenerated research recipe for producing a data item containing research data.
     *
     * @param researchId    the id of the research to store
     * @param researchStack the stack to scan for research
     * @param dataStack     the stack to contain the data
     * @param duration      the duration of the recipe
     * @param EUt           the EUt of the recipe
     * @param CWUt          how much computation per tick this recipe needs if in Research Station
     */
    public record ResearchRecipeEntry(
                                      @NotNull String researchId,
                                      @NotNull ItemStack researchStack,
                                      @NotNull ItemStack dataStack,
                                      int duration,
                                      int EUt,
                                      int CWUt) {

    }
}
