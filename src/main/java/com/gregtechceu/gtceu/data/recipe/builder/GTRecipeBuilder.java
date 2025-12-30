package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
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
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.recipe.condition.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

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
    public int duration = 100;
    @Setter
    public boolean perTick;
    @Setter
    public int chance = ChanceLogic.getMaxChancedValue();
    @Setter
    public int maxChance = ChanceLogic.getMaxChancedValue();
    @Setter
    public int tierChanceBoost = 0;
    private boolean itemMaterialInfo = false;
    private boolean fluidMaterialInfo = false;
    private boolean removePreviousMatInfo = false;
    public GTRecipeCategory recipeCategory;
    @Setter
    public @Nullable BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave;

    @Getter
    private final Collection<ResearchRecipeEntry> researchRecipeEntries = new ArrayList<>();
    private boolean generatingRecipes = true;

    // material stacks that are from already resolved inputs
    private List<ItemStack> tempItemStacks = new ArrayList<>();
    private List<MaterialStack> tempItemMaterialStacks = new ArrayList<>();
    // temporary buffer for unresolved item stacks where decomp is found post recipe addition
    private List<MaterialStack> tempFluidStacks = new ArrayList<>();

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
        copy.recipeCategory = this.recipeCategory;
        copy.onSave = this.onSave;
        return copy;
    }

    public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        recipeType.setMinRecipeConditions(builder.conditions.size());
        return builder.copy(builder.id).onSave(null).recipeType(recipeType).category(recipeCategory);
    }

    protected Content makeContent(Object o) {
        return new Content(o, chance, maxChance, tierChanceBoost);
    }

    public <T> GTRecipeBuilder input(RecipeCapability<T> capability, T obj) {
        var t = (perTick ? tickInput : input);
        warnTooManyIngredients(capability, true, t, 1);
        t.computeIfAbsent(capability, c -> new ArrayList<>()).add(makeContent(capability.of(obj)));
        return this;
    }

    public <T> GTRecipeBuilder input(RecipeCapability<T> capability, T... obj) {
        var t = (perTick ? tickInput : input);
        warnTooManyIngredients(capability, true, t, obj.length);
        t.computeIfAbsent(capability, c -> new ArrayList<>())
                .addAll(Arrays.stream(obj).map(capability::of).map(this::makeContent).toList());
        return this;
    }

    public <T> GTRecipeBuilder output(RecipeCapability<T> capability, T obj) {
        var t = (perTick ? tickOutput : output);
        warnTooManyIngredients(capability, false, t, 1);
        t.computeIfAbsent(capability, c -> new ArrayList<>()).add(makeContent(capability.of(obj)));
        return this;
    }

    public <T> GTRecipeBuilder output(RecipeCapability<T> capability, T... obj) {
        var t = (perTick ? tickOutput : output);
        warnTooManyIngredients(capability, false, t, obj.length);
        t.computeIfAbsent(capability, c -> new ArrayList<>())
                .addAll(Arrays.stream(obj).map(capability::of).map(this::makeContent).toList());
        return this;
    }

    public GTRecipeBuilder addCondition(RecipeCondition condition) {
        conditions.add(condition);
        recipeType.setMinRecipeConditions(conditions.size());
        return this;
    }

    public GTRecipeBuilder duration(int duration) {
        if (duration < 0) {
            GTCEu.LOGGER.error("Recipe duration must be non negative, id: {}", this.id);
        }
        this.duration = Math.max(duration, 0);
        return this;
    }

    public GTRecipeBuilder inputEU(long eu) {
        return inputEU(eu, 1);
    }

    public GTRecipeBuilder inputEU(long voltage, long amperage) {
        return input(EURecipeCapability.CAP, new EnergyStack(voltage, amperage));
    }

    public GTRecipeBuilder EUt(long eu) {
        return EUt(eu, 1);
    }

    public GTRecipeBuilder EUt(long voltage, long amperage) {
        if (voltage == 0) {
            GTCEu.LOGGER.error("EUt can't be explicitly set to 0, id: {}", id);
        }
        if (amperage < 1) {
            GTCEu.LOGGER.error("Amperage must be a positive integer, id: {}", id);
        }
        var lastPerTick = perTick;
        perTick = true;
        if (voltage > 0) {
            tickInput.remove(EURecipeCapability.CAP);
            inputEU(voltage, amperage);
        } else if (voltage < 0) {
            tickOutput.remove(EURecipeCapability.CAP);
            outputEU(-voltage, amperage);
        }
        perTick = lastPerTick;
        return this;
    }

    public GTRecipeBuilder outputEU(long eu) {
        return outputEU(eu, 1);
    }

    public GTRecipeBuilder outputEU(long voltage, long amperage) {
        return output(EURecipeCapability.CAP, new EnergyStack(voltage, amperage));
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
            outputCWU(-cwu);
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
        } else if (input instanceof MaterialEntry entry) {
            return inputItems(entry);
        } else if (input instanceof TagKey<?> tag) {
            return inputItems((TagKey<Item>) tag);
        } else if (input instanceof MachineDefinition machine) {
            return inputItems(machine);
        } else {
            GTCEu.LOGGER.error("""
                    Input item is not one of:
                    Item, Supplier<Item>, ItemStack, Ingredient, MaterialEntry, TagKey<Item>, MachineDefinition
                    id: {}""", id);
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
        } else if (input instanceof MaterialEntry entry) {
            return inputItems(entry, count);
        } else if (input instanceof TagKey<?> tag) {
            return inputItems((TagKey<Item>) tag, count);
        } else if (input instanceof MachineDefinition machine) {
            return inputItems(machine, count);
        } else {
            GTCEu.LOGGER.error("""
                    Input item is not one of:
                    Item, Supplier<Item>, ItemStack, Ingredient, MaterialEntry, TagKey<Item>, MachineDefinition
                    id: {}""", id);
            return this;
        }
    }

    public GTRecipeBuilder inputItems(Ingredient inputs) {
        if (missingIngredientError(0, true, ItemRecipeCapability.CAP, inputs::isEmpty)) {
            return this;
        }
        return input(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder inputItems(Ingredient... inputs) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < inputs.length; i++) {
            var ingredient = inputs[i];
            if (missingIngredientError(i, true, ItemRecipeCapability.CAP, ingredient::isEmpty)) {
                return this;
            } else {
                ingredients.add(ingredient);
            }
        }
        return input(ItemRecipeCapability.CAP, ingredients.toArray(Ingredient[]::new));
    }

    public GTRecipeBuilder inputItems(Ingredient inputs, int count) {
        if (missingIngredientError(0, true, ItemRecipeCapability.CAP, inputs::isEmpty)) {
            return this;
        }
        return input(ItemRecipeCapability.CAP, SizedIngredient.create(inputs, count));
    }

    public GTRecipeBuilder inputItems(ItemStack input) {
        if (missingIngredientError(0, true, ItemRecipeCapability.CAP, input::isEmpty)) {
            return this;
        }
        gatherMaterialInfoFromStack(input);
        return input(ItemRecipeCapability.CAP, SizedIngredient.create(input));
    }

    public GTRecipeBuilder inputItems(ItemStack... inputs) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < inputs.length; i++) {
            ItemStack itemStack = inputs[i];
            if (missingIngredientError(i, true, ItemRecipeCapability.CAP, itemStack::isEmpty)) {
                return this;
            } else {
                gatherMaterialInfoFromStack(itemStack);
                ingredients.add(SizedIngredient.create(itemStack));
            }
        }
        return input(ItemRecipeCapability.CAP, ingredients.toArray(Ingredient[]::new));
    }

    public GTRecipeBuilder inputItems(TagKey<Item> tag, int amount) {
        return inputItems(SizedIngredient.create(tag, amount));
    }

    public GTRecipeBuilder inputItems(TagKey<Item> tag) {
        return inputItems(tag, 1);
    }

    public GTRecipeBuilder inputItems(Item input, int amount) {
        return inputItems(new ItemStack(input, amount));
    }

    public GTRecipeBuilder inputItems(Item input) {
        return inputItems(input, 1);
    }

    public GTRecipeBuilder inputItems(Supplier<? extends Item> input) {
        return inputItems(input.get());
    }

    public GTRecipeBuilder inputItems(Supplier<? extends Item> input, int amount) {
        return inputItems(input.get(), amount);
    }

    public GTRecipeBuilder inputItems(TagPrefix orePrefix, Material material) {
        return inputItems(orePrefix, material, 1);
    }

    public GTRecipeBuilder inputItems(MaterialEntry input) {
        return inputItems(input, 1);
    }

    public GTRecipeBuilder inputItems(MaterialEntry input, int count) {
        return inputItems(input.tagPrefix(), input.material(), count);
    }

    public GTRecipeBuilder inputItems(TagPrefix tagPrefix, @NotNull Material material, int count) {
        if (tagPrefix.isEmpty() || material.isNull()) {
            GTCEu.LOGGER.error(
                    "Tried to set input item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}",
                    id, tagPrefix, material, count);
            return this;
        } else {
            tempItemMaterialStacks.add(new MaterialStack(material, tagPrefix.getMaterialAmount(material) * count));
            tagPrefix.secondaryMaterials().forEach(mat -> tempItemMaterialStacks.add(mat.multiply(count)));
        }
        TagKey<Item> tag = ChemicalHelper.getTag(tagPrefix, material);
        if (tag != null) {
            return inputItems(tag, count);
        } else {
            var item = ChemicalHelper.get(tagPrefix, material, count);
            if (item.isEmpty()) {
                GTCEu.LOGGER.error(
                        "Tried to set input item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}",
                        id, tagPrefix, material, count);
            }
            return input(ItemRecipeCapability.CAP, SizedIngredient.create(item));
        }
    }

    public GTRecipeBuilder inputItems(MachineDefinition machine) {
        return inputItems(machine, 1);
    }

    public GTRecipeBuilder inputItems(MachineDefinition machine, int count) {
        return inputItems(machine.asStack(count));
    }

    public GTRecipeBuilder inputItemRanged(IntProviderIngredient provider) {
        return inputItems(provider);
    }

    public GTRecipeBuilder inputItemsRanged(ItemStack input, IntProvider intProvider) {
        return inputItemRanged(IntProviderIngredient.of(input, intProvider));
    }

    public GTRecipeBuilder inputItemsRanged(Item input, IntProvider intProvider) {
        return inputItemsRanged(new ItemStack(input), intProvider);
    }

    public GTRecipeBuilder inputItemsRanged(Supplier<? extends ItemLike> input, IntProvider intProvider) {
        return inputItemsRanged(new ItemStack(input.get().asItem()), intProvider);
    }

    public GTRecipeBuilder inputItemsRanged(TagPrefix orePrefix, Material material, IntProvider intProvider) {
        var item = ChemicalHelper.get(orePrefix, material, 1);
        if (item.isEmpty()) {
            GTCEu.LOGGER.error("Tried to set input ranged item stack that doesn't exist, TagPrefix: {}, Material: {}",
                    orePrefix, material);
        }
        return inputItemsRanged(item, intProvider);
    }

    public GTRecipeBuilder inputItemsRanged(MachineDefinition machine, IntProvider intProvider) {
        return inputItemsRanged(machine.asStack(), intProvider);
    }

    public GTRecipeBuilder inputItemNbtPredicate(ItemStack stack, NBTPredicate predicate) {
        if (missingIngredientError(0, true, ItemRecipeCapability.CAP, stack::isEmpty)) {
            return this;
        }
        gatherMaterialInfoFromStack(stack);
        return inputItems(NBTPredicateIngredient.of(stack, predicate));
    }

    public GTRecipeBuilder outputItems(Object output) {
        if (output instanceof Item item) {
            return outputItems(item);
        } else if (output instanceof Supplier<?> supplier && supplier.get() instanceof ItemLike item) {
            return outputItems(item.asItem());
        } else if (output instanceof ItemStack stack) {
            return outputItems(stack);
        } else if (output instanceof MaterialEntry entry) {
            return outputItems(entry);
        } else if (output instanceof MachineDefinition machine) {
            return outputItems(machine);
        } else {
            GTCEu.LOGGER.error("""
                    Output item is not one of:
                    Item, Supplier<Item>, ItemStack, MaterialEntry, MachineDefinition
                    id: {}""", id);
            return this;
        }
    }

    public GTRecipeBuilder outputItems(Object output, int count) {
        if (output instanceof Item item) {
            return outputItems(item, count);
        } else if (output instanceof Supplier<?> supplier && supplier.get() instanceof ItemLike item) {
            return outputItems(item.asItem(), count);
        } else if (output instanceof ItemStack stack) {
            return outputItems(stack.copyWithCount(count));
        } else if (output instanceof MaterialEntry entry) {
            return outputItems(entry, count);
        } else if (output instanceof MachineDefinition machine) {
            return outputItems(machine, count);
        } else {
            GTCEu.LOGGER.error("""
                    Output item is not one of:
                    Item, Supplier<Item>, ItemStack, MaterialEntry, MachineDefinition
                    id: {}""", id);
            return this;
        }
    }

    public GTRecipeBuilder outputItems(ItemStack output) {
        if (missingIngredientError(0, false, ItemRecipeCapability.CAP, output::isEmpty)) {
            return this;
        }
        return output(ItemRecipeCapability.CAP, SizedIngredient.create(output));
    }

    public GTRecipeBuilder outputItems(ItemStack... outputs) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < outputs.length; i++) {
            ItemStack itemStack = outputs[i];
            if (missingIngredientError(i, false, ItemRecipeCapability.CAP, itemStack::isEmpty)) {
                return this;
            } else {
                ingredients.add(SizedIngredient.create(itemStack));
            }
        }
        return output(ItemRecipeCapability.CAP, ingredients.toArray(Ingredient[]::new));
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

    public GTRecipeBuilder outputItems(TagPrefix orePrefix, @NotNull Material material, int count) {
        if (orePrefix.isEmpty() || material.isNull()) {
            GTCEu.LOGGER.error(
                    "Tried to set output item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}",
                    id, orePrefix, material, count);
            return this;
        }
        var item = ChemicalHelper.get(orePrefix, material, count);
        if (item.isEmpty()) {
            GTCEu.LOGGER.error(
                    "Tried to set output item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}",
                    id, orePrefix, material, count);
            return this;
        }
        return outputItems(item);
    }

    public GTRecipeBuilder outputItems(MaterialEntry entry) {
        return outputItems(entry.tagPrefix(), entry.material());
    }

    public GTRecipeBuilder outputItems(MaterialEntry entry, int count) {
        return outputItems(entry.tagPrefix(), entry.material(), count);
    }

    public GTRecipeBuilder outputItems(MachineDefinition machine) {
        return outputItems(machine, 1);
    }

    public GTRecipeBuilder outputItems(MachineDefinition machine, int count) {
        return outputItems(machine.asStack(count));
    }

    protected GTRecipeBuilder outputItems(Ingredient ingredient) {
        return output(ItemRecipeCapability.CAP, ingredient);
    }

    public GTRecipeBuilder outputItemRanged(IntProviderIngredient provider) {
        return outputItems(provider);
    }

    public GTRecipeBuilder outputItemsRanged(ItemStack output, IntProvider intProvider) {
        return outputItemRanged(IntProviderIngredient.of(output, intProvider));
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
        return notConsumable(IntCircuitIngredient.of(configuration));
    }

    public GTRecipeBuilder chancedInput(Ingredient stack, int chance, int tierChanceBoost) {
        if (checkChanceAndPrintError(chance)) {
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

    public GTRecipeBuilder chancedInput(FluidIngredient stack, int chance, int tierChanceBoost) {
        if (checkChanceAndPrintError(chance)) {
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

    public GTRecipeBuilder chancedOutput(Ingredient stack, int chance, int tierChanceBoost) {
        if (checkChanceAndPrintError(chance)) {
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

    public GTRecipeBuilder chancedOutput(FluidIngredient stack, int chance, int tierChanceBoost) {
        if (checkChanceAndPrintError(chance)) {
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

    public GTRecipeBuilder chancedInput(ItemStack stack, int chance, int tierChanceBoost) {
        return chancedInput(Ingredient.of(stack), chance, tierChanceBoost);
    }

    public GTRecipeBuilder chancedInput(FluidStack stack, int chance, int tierChanceBoost) {
        return chancedInput(FluidIngredient.of(stack), chance, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
        return chancedOutput(Ingredient.of(stack), chance, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(FluidStack stack, int chance, int tierChanceBoost) {
        return chancedOutput(FluidIngredient.of(stack), chance, tierChanceBoost);
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

    public GTRecipeBuilder inputFluids(@NotNull Material material, int amount) {
        return inputFluids(material.getFluid(amount));
    }

    public GTRecipeBuilder inputFluids(FluidStack input) {
        if (missingIngredientError(0, true, FluidRecipeCapability.CAP, input::isEmpty)) {
            return this;
        }
        var matStack = ChemicalHelper.getMaterial(input.getFluid());
        if (!matStack.isNull() && chance != 0 && chance == maxChance) {
            tempFluidStacks.add(new MaterialStack(matStack, input.getAmount() * GTValues.M / GTValues.L));
        }
        return input(FluidRecipeCapability.CAP, FluidIngredient.of(
                TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(input.getFluid()).getPath()),
                input.getAmount(), input.getTag()));
    }

    public GTRecipeBuilder inputFluids(FluidStack... inputs) {
        List<FluidIngredient> ingredients = new ArrayList<>();
        for (int i = 0; i < inputs.length; i++) {
            FluidStack fluid = inputs[i];
            if (missingIngredientError(i, true, FluidRecipeCapability.CAP, fluid::isEmpty)) {
                return this;
            } else {
                var matStack = ChemicalHelper.getMaterial(fluid.getFluid());
                if (!matStack.isNull()) {
                    if (chance == maxChance && chance != 0) {
                        tempFluidStacks.add(new MaterialStack(matStack, fluid.getAmount() * GTValues.M / GTValues.L));
                    }
                }

                TagKey<Fluid> tag = TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath());
                ingredients.add(FluidIngredient.of(tag, fluid.getAmount(), fluid.getTag()));
            }
        }
        return input(FluidRecipeCapability.CAP, ingredients.toArray(FluidIngredient[]::new));
    }

    public GTRecipeBuilder inputFluidsRanged(IntProviderFluidIngredient provider) {
        return inputFluids(provider);
    }

    protected GTRecipeBuilder inputFluidsRanged(FluidIngredient input, IntProvider intProvider) {
        return inputFluidsRanged(IntProviderFluidIngredient.of(input, intProvider));
    }

    public GTRecipeBuilder inputFluidsRanged(FluidStack input, IntProvider intProvider) {
        return inputFluidsRanged(FluidIngredient.of(input), intProvider);
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

    public GTRecipeBuilder outputFluidsRanged(IntProviderFluidIngredient provider) {
        return outputFluids(provider);
    }

    protected GTRecipeBuilder outputFluidsRanged(FluidIngredient output, IntProvider intProvider) {
        return outputFluidsRanged(IntProviderFluidIngredient.of(output, intProvider));
    }

    public GTRecipeBuilder outputFluidsRanged(FluidStack output, IntProvider intProvider) {
        return outputFluidsRanged(FluidIngredient.of(output), intProvider);
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

    public GTRecipeBuilder addData(String key, float data) {
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
        return biome(ResourceKey.create(Registries.BIOME, biome), reverse);
    }

    public GTRecipeBuilder biome(ResourceLocation biome) {
        return biome(biome, false);
    }

    public GTRecipeBuilder biome(ResourceKey<Biome> biome, boolean reverse) {
        return addCondition(new BiomeCondition(biome).setReverse(reverse));
    }

    public GTRecipeBuilder biome(ResourceKey<Biome> biome) {
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

    public final GTRecipeBuilder adjacentFluids(Fluid... fluids) {
        return adjacentFluids(false, fluids);
    }

    public final GTRecipeBuilder adjacentFluids(boolean isReverse, Fluid... fluids) {
        if (fluids.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many fluids, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(AdjacentFluidCondition.fromFluids(fluids).setReverse(isReverse));
    }

    public final GTRecipeBuilder adjacentFluid(Fluid... fluids) {
        return adjacentFluid(false, fluids);
    }

    public final GTRecipeBuilder adjacentFluid(boolean isReverse, Fluid... fluids) {
        if (fluids.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many fluids, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(AdjacentFluidCondition.fromFluids(fluids).setReverse(isReverse));
    }

    @SafeVarargs
    public final GTRecipeBuilder adjacentFluidTag(TagKey<Fluid>... tags) {
        return adjacentFluidTag(false, tags);
    }

    @SafeVarargs
    public final GTRecipeBuilder adjacentFluidTag(boolean isReverse, TagKey<Fluid>... tags) {
        if (tags.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many fluids, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(AdjacentFluidCondition.fromTags(tags).setReverse(isReverse));
    }

    @SafeVarargs
    public final GTRecipeBuilder adjacentFluid(TagKey<Fluid>... tags) {
        return adjacentFluid(false, tags);
    }

    @SafeVarargs
    public final GTRecipeBuilder adjacentFluid(boolean isReverse, TagKey<Fluid>... tags) {
        if (tags.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many fluids, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(AdjacentFluidCondition.fromTags(tags).setReverse(isReverse));
    }

    public GTRecipeBuilder adjacentFluid(Collection<HolderSet<Fluid>> fluids) {
        return adjacentFluid(fluids, false);
    }

    public GTRecipeBuilder adjacentFluid(Collection<HolderSet<Fluid>> fluids, boolean isReverse) {
        if (fluids.size() > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many fluids, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(new AdjacentFluidCondition(isReverse, new ArrayList<>(fluids)));
    }

    public GTRecipeBuilder adjacentBlocks(Block... blocks) {
        return adjacentBlocks(false, blocks);
    }

    public GTRecipeBuilder adjacentBlocks(boolean isReverse, Block... blocks) {
        if (blocks.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many blocks, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(AdjacentBlockCondition.fromBlocks(blocks).setReverse(isReverse));
    }

    public GTRecipeBuilder adjacentBlock(Block... blocks) {
        return adjacentBlock(false, blocks);
    }

    public GTRecipeBuilder adjacentBlock(boolean isReverse, Block... blocks) {
        if (blocks.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many blocks, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(AdjacentBlockCondition.fromBlocks(blocks).setReverse(isReverse));
    }

    @SafeVarargs
    public final GTRecipeBuilder adjacentBlock(TagKey<Block>... tags) {
        return adjacentBlock(false, tags);
    }

    @SafeVarargs
    public final GTRecipeBuilder adjacentBlock(boolean isReverse, TagKey<Block>... tags) {
        if (tags.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many blocks, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(AdjacentBlockCondition.fromTags(tags).setReverse(isReverse));
    }

    @SafeVarargs
    public final GTRecipeBuilder adjacentBlockTag(TagKey<Block>... tags) {
        return adjacentBlockTag(false, tags);
    }

    @SafeVarargs
    public final GTRecipeBuilder adjacentBlockTag(boolean isReverse, TagKey<Block>... tags) {
        if (tags.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many blocks, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(AdjacentBlockCondition.fromTags(tags).setReverse(isReverse));
    }

    public GTRecipeBuilder adjacentBlock(Collection<HolderSet<Block>> blocks) {
        return adjacentBlock(blocks, false);
    }

    public GTRecipeBuilder adjacentBlock(Collection<HolderSet<Block>> blocks, boolean isReverse) {
        if (blocks.size() > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
            GTCEu.LOGGER.error("Has too many blocks, not adding to recipe, id: {}", this.id);
            return this;
        }
        return addCondition(new AdjacentBlockCondition(isReverse, new ArrayList<>(blocks)));
    }

    public GTRecipeBuilder daytime(boolean isNight) {
        return addCondition(new DaytimeCondition().setReverse(isNight));
    }

    public GTRecipeBuilder daytime() {
        return daytime(false);
    }

    public GTRecipeBuilder heraclesQuest(String questId, boolean isReverse) {
        if (!GTCEu.Mods.isHeraclesLoaded()) {
            GTCEu.LOGGER.error("Heracles not loaded!");
            return this;
        }
        if (questId.isEmpty()) {
            GTCEu.LOGGER.error("Quest ID cannot be empty for recipe {}", this.id);
            return this;
        }
        return addCondition(new HeraclesQuestCondition(isReverse, questId));
    }

    public GTRecipeBuilder heraclesQuest(String questId) {
        return heraclesQuest(questId, false);
    }

    public GTRecipeBuilder gameStage(String stageName) {
        return gameStage(stageName, false);
    }

    public GTRecipeBuilder gameStage(String stageName, boolean isReverse) {
        if (!GTCEu.Mods.isGameStagesLoaded()) {
            GTCEu.LOGGER.warn("GameStages is not loaded, ignoring recipe condition");
            return this;
        }
        return addCondition(new GameStageCondition(isReverse, stageName));
    }

    public GTRecipeBuilder ftbQuest(String questId, boolean isReverse) {
        if (!GTCEu.Mods.isFTBQuestsLoaded()) {
            GTCEu.LOGGER.error("FTBQuests is not loaded!");
            return this;
        }
        if (questId.isEmpty()) {
            GTCEu.LOGGER.error("Quest ID cannot be empty for recipe {}", this.id);
            return this;
        }
        long qID = QuestObjectBase.parseCodeString(questId);
        if (qID == 0L) {
            GTCEu.LOGGER.error("Quest {} not found for recipe {}", questId, this.id);
            return this;
        }
        return addCondition(new FTBQuestCondition(isReverse, qID));
    }

    public GTRecipeBuilder ftbQuest(String questId) {
        return ftbQuest(questId, false);
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
        ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.ScannerRecipeBuilder()).build(this.id);
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
        ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.StationRecipeBuilder()).build(this.id);
        if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId, entry.dataStack))) {
            this.researchRecipeEntries.add(entry);
        }
        return this;
    }

    public GTRecipeBuilder category(@NotNull GTRecipeCategory category) {
        this.recipeCategory = category;
        return this;
    }

    public GTRecipeBuilder addMaterialInfo(boolean item) {
        this.itemMaterialInfo = item;
        return this;
    }

    public GTRecipeBuilder addMaterialInfo(boolean item, boolean fluid) {
        this.itemMaterialInfo = item;
        this.fluidMaterialInfo = fluid;
        return this;
    }

    public GTRecipeBuilder removePreviousMaterialInfo() {
        removePreviousMatInfo = true;
        return this;
    }

    public GTRecipeBuilder setTempItemMaterialStacks(List<MaterialStack> stacks) {
        tempItemMaterialStacks = stacks;
        return this;
    }

    public GTRecipeBuilder setTempFluidMaterialStacks(List<MaterialStack> stacks) {
        tempFluidStacks = stacks;
        return this;
    }

    public GTRecipeBuilder setTempItemStacks(List<ItemStack> stacks) {
        tempItemStacks = stacks;
        return this;
    }

    public void toJson(JsonObject json) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        JsonObject serialized = GTRecipeSerializer.CODEC.encodeStart(ops, buildRawRecipe())
                .getOrThrow(false, GTCEu.LOGGER::error).getAsJsonObject();
        for (String key : serialized.keySet()) {
            json.add(key, serialized.get(key));
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

        if (removePreviousMatInfo) {
            removeExistingMaterialInfo();
        }

        if (itemMaterialInfo || fluidMaterialInfo) {
            addOutputMaterialInfo();
        }

        tempItemStacks = null;
        tempItemMaterialStacks = null;
        tempFluidStacks = null;

        consumer.accept(build());
    }

    private void gatherMaterialInfoFromStack(ItemStack input) {
        var matInfo = ItemMaterialData.getMaterialInfo(input.getItem());
        var unresolvedMatInfo = ItemMaterialData.UNRESOLVED_ITEM_MATERIAL_INFO.get(input);
        if (chance == maxChance && chance != 0) {
            if (unresolvedMatInfo != null) {
                tempItemStacks.add(input);
            }
            if (matInfo != null) {
                for (var matStack : matInfo.getMaterials()) {
                    tempItemMaterialStacks.add(matStack.multiply(input.getCount()));
                }
            } else if (unresolvedMatInfo == null) {
                tempItemStacks.add(input);
            }

        }
    }

    private void addOutputMaterialInfo() {
        var itemOutputs = output.getOrDefault(ItemRecipeCapability.CAP, new ArrayList<>());
        var itemInputs = input.getOrDefault(ItemRecipeCapability.CAP, new ArrayList<>());
        if (itemOutputs.size() == 1 && (!itemInputs.isEmpty() || !tempFluidStacks.isEmpty())) {
            var currOutput = ItemRecipeCapability.CAP.of(itemOutputs.get(0).content);
            Item out = null;
            int outputCount = 0;

            if (currOutput instanceof IntProviderIngredient intProvider) {
                ItemStack[] items = intProvider.getInner().getItems();
                if (items.length > 0) {
                    out = items[0].getItem();
                    // use the max amount of items for decomp info so dupes can't happen
                    outputCount = intProvider.getCountProvider().getMaxValue();
                }
            } else if (!currOutput.isEmpty()) {
                ItemStack[] items = currOutput.getItems();
                if (items.length > 0) {
                    out = items[0].getItem();
                    outputCount = items[0].getCount();
                }
            }

            if (out == null || out == Items.AIR) {
                return;
            }

            Reference2LongOpenHashMap<Material> matStacks = new Reference2LongOpenHashMap<>();
            if (itemMaterialInfo) {
                for (var input : tempItemMaterialStacks) {
                    long am = input.amount() / outputCount;
                    matStacks.addTo(input.material(), am);
                }
            }

            if (fluidMaterialInfo) {
                for (var input : tempFluidStacks) {
                    long am = input.amount() / outputCount;
                    matStacks.addTo(input.material(), am);
                }
            }

            if (outputCount != 0 && !tempItemStacks.isEmpty()) {
                ItemMaterialData.UNRESOLVED_ITEM_MATERIAL_INFO.put(new ItemStack(out, outputCount), tempItemStacks);
            }

            if (!matStacks.isEmpty()) {
                ItemMaterialData.registerMaterialInfo(out, new ItemMaterialInfo(matStacks));
            }
        }
    }

    private void removeExistingMaterialInfo() {
        var itemOutputs = output.get(ItemRecipeCapability.CAP);
        if (itemOutputs.size() == 1) {
            var currOutput = ItemRecipeCapability.CAP.of(itemOutputs.get(0).content);
            Item out = null;
            int outputCount = 0;

            if (currOutput instanceof IntProviderIngredient intProvider) {
                ItemStack[] items = intProvider.getInner().getItems();
                if (items.length > 0) {
                    out = items[0].getItem();
                    // use the max amount of items for decomp info so dupes can't happen
                    outputCount = intProvider.getCountProvider().getMaxValue();
                }
            } else if (!currOutput.isEmpty()) {
                ItemStack[] items = currOutput.getItems();
                if (items.length > 0) {
                    out = items[0].getItem();
                    outputCount = items[0].getCount();
                }
            }

            if (out == null || out == Items.AIR) {
                return;
            }

            if (outputCount != 0) {
                ItemMaterialData.UNRESOLVED_ITEM_MATERIAL_INFO.remove(new ItemStack(out, outputCount));
            }

            var existingItemInfo = ItemMaterialData.getMaterialInfo(out);
            if (existingItemInfo != null) {
                ItemMaterialData.clearMaterialInfo(out);
            }
        }
    }

    public GTRecipe buildRawRecipe() {
        return new GTRecipe(recipeType, id.withPrefix(recipeType.registryName.getPath() + "/"),
                input, output, tickInput, tickOutput,
                inputChanceLogic, outputChanceLogic, tickInputChanceLogic, tickOutputChanceLogic,
                conditions, List.of(), data, duration, recipeCategory);
    }

    protected void warnTooManyIngredients(RecipeCapability<?> capability,
                                          boolean isInput,
                                          Map<RecipeCapability<?>, List<Content>> table,
                                          int addedEntries) {
        var recipeCapabilityMax = isInput ? recipeType.maxInputs : recipeType.maxOutputs;
        if (!recipeCapabilityMax.containsKey(capability)) return;

        int max = recipeCapabilityMax.getInt(capability);
        if (table.getOrDefault(capability, List.of()).size() + addedEntries > max) {
            String io = isInput ? "inputs" : "outputs";
            GTCEu.LOGGER.warn("Recipe {} is trying to add more {} than its recipe type can support, Max {} {}: {}",
                    id, io, capability.name, io, max);
        }
    }

    protected boolean missingIngredientError(int index, boolean isInput,
                                             RecipeCapability<?> cap, BooleanSupplier empty) {
        if (empty.getAsBoolean()) {
            String io = isInput ? "Input" : "Output";
            if (perTick) {
                io = "Tick " + io.toLowerCase(Locale.ROOT);
            }
            int size = (perTick ? tickOutput : output).getOrDefault(cap, List.of()).size();
            GTCEu.LOGGER.error("{} {} {} of recipe {} is empty", io, cap.name, size + index, id);
            return true;
        }
        return false;
    }

    protected boolean checkChanceAndPrintError(int chance) {
        if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
            GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                    ChanceLogic.getMaxChancedValue(), chance, new Throwable());
            return true;
        }
        return false;
    }

    //////////////////////////////////////
    // ******* Quick Query *******//
    //////////////////////////////////////
    public EnergyStack EUt() {
        if (!tickInput.containsKey(EURecipeCapability.CAP)) return EnergyStack.EMPTY;
        if (tickInput.get(EURecipeCapability.CAP).isEmpty()) return EnergyStack.EMPTY;
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
     * @param researchItem  the item stack to scan for research
     * @param researchFluid the fluid stack to scan for research
     * @param dataStack     the stack to contain the data
     * @param duration      the duration of the recipe
     * @param EUt           the EUt of the recipe
     * @param CWUt          how much computation per tick this recipe needs if in Research Station
     */
    public record ResearchRecipeEntry(@NotNull String researchId,
                                      @NotNull ItemStack researchItem, @NotNull FluidStack researchFluid,
                                      @NotNull ItemStack dataStack, int duration, EnergyStack EUt, int CWUt) {

    }
}
